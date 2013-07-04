package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.apache.commons.lang.StringUtils.isNotBlank

class BuildVariantTask extends DefaultTask {

    String group = FLOW_BUILD
    String description = 'Builds single variant for iOS'

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSExecutor executor
    @Inject PlistParser plistParser
    @Inject IOSArtifactProvider artifactProvider
    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder

    IOSVariant variant

    @TaskAction
    void buildVariant() {

        if (variant != null) {
            build()
            if (releaseConf.enabled) {
                def bi = artifactProvider.builderInfo(variant)
                switch (bi.mode) {
                    case DEVICE:
                        deviceArtifactsBuilder.buildArtifacts(bi)
                        break

                    case SIMULATOR:
                        simulatorArtifactsBuilder.buildArtifacts(bi)
                        break
                    default:
                        logger.warn("Unrecognized mode: $bi.mode, builder info: $bi")
                }
            }
        } else
            logger.info('Variant builder not executed - null variant passed')
    }

    private void build() {
        def newBundleId = variant.bundleId.value
        if (isNotBlank(newBundleId)) {
            def oldBundleId = plistParser.bundleId(variant.plist)
            plistParser.replaceBundledId(variant.plist, oldBundleId, newBundleId)
            replaceBundleInAllSourceFiles(variant.tmpDir, oldBundleId, newBundleId)
        }
        executor.buildVariant(variant.tmpDir, variant.buildCmd)
    }

    private void replaceBundleInAllSourceFiles(File dir, String newBundleId, String oldBundleId) {
        String valueToFind = 'bundleWithIdentifier:@"' + oldBundleId
        String valueToReplace = 'bundleWithIdentifier:@"' + newBundleId
        findAllSourceFiles(dir).each { file ->
            String t = file.text
            if (t.contains(valueToFind)) {
                file.write(t.replace(valueToFind, valueToReplace))
                logger.info("Replaced the $valueToFind with $valueToReplace in $file")
            }
        }
    }

    private Collection<File> findAllSourceFiles(File dir) {
        def result = []
        dir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if ((it.name.endsWith('.m') || it.name.endsWith('.h')) && !it.path.contains('/External/')) {
                logger.info("Adding source file ${it} to processing list")
                result << it
            }
        }
        result
    }
}
