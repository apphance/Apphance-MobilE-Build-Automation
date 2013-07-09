package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.ARCHIVE_ACTION
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

class ArchiveVariantTask extends DefaultTask {

    String description = "Executes 'archive' action for single variant"
    String group = FLOW_BUILD

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder
    @Inject IOSExecutor executor
    @Inject XCSchemeParser schemeParser

    IOSVariant variant

    @TaskAction
    void build() {
        checkNotNull(variant, 'Null variant passed to builder!')
        logger.info("Adding post archive action to scheme file: $variant.schemeFile.absolutePath")
        schemeParser.addPostArchiveAction(variant.schemeFile)
        def archiveOutput = executor.archiveVariant(variant.tmpDir, variant.archiveCmd)

        if (releaseConf.enabled) {
            def bi = artifactProvider.builderInfo(variant)
            bi.archiveDir = findArchiveFile(archiveOutput)
            bi.appName = appName()
            bi.productName = productName()

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
    }

    private File findArchiveFile(Iterator<String> compilerOutput) {
        def line = compilerOutput.find { it.contains('FLOW_ARCHIVE_PATH') }
        def file = line ? new File(line.split('=')[1].trim()) : null
        logger.info("Found xcarchive file: ${file?.absolutePath}")
        checkArgument(file?.exists() && file?.isDirectory(), "Xcarchive file: $file.absolutePath does not exist or is not a directory")
        file
    }

    private String appName() {
        executor.buildSettings(variant.target, schemeParser.configuration(variant.schemeFile, ARCHIVE_ACTION))['FULL_PRODUCT_NAME']
    }

    private String productName() {
        executor.buildSettings(variant.target, schemeParser.configuration(variant.schemeFile, ARCHIVE_ACTION))['PRODUCT_NAME']
    }
}
