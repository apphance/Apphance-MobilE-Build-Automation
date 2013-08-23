package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.AbstractIOSArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

class ArchiveVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'archive' action for single variant"

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder
    @Inject XCSchemeParser schemeParser

    @Override
    void build() {
        checkNotNull(variant, 'Null variant passed to builder!')
        logger.info("Adding post archive action to scheme file: $variant.schemeFile.absolutePath")
        schemeParser.addPostArchiveAction(variant.schemeFile)
        def archiveOutput = iosExecutor.buildVariant(variant.tmpDir, cmd)

        if (releaseConf.enabled) {
            def bi = artifactProvider.builderInfo(variant)
            bi.archiveDir = findArchiveFile(archiveOutput)
            bi.appName = appName()
            bi.productName = productName()

            builder.call().buildArtifacts(bi)
        }
    }

    @Lazy
    List<String> cmd = {
        (conf.xcodebuildExecutionPath() + ['-scheme', variant.name] + sdkCmd + archCmd + ['clean', 'archive'])
    }()

    @PackageScope
    File findArchiveFile(Iterator<String> compilerOutput) {
        def line = compilerOutput.find { it.contains('FLOW_ARCHIVE_PATH') }
        def file = line ? new File(line.split('=')[1].trim()) : null
        logger.info("Found xcarchive file: ${file?.absolutePath}")
        checkArgument(file?.exists() && file?.isDirectory(), "Xcarchive file: $file.absolutePath does not exist or is not a directory")
        file
    }

    private String appName() {
        iosExecutor.buildSettings(variant.target, variant.archiveConfiguration)['FULL_PRODUCT_NAME']
    }

    private String productName() {
        iosExecutor.buildSettings(variant.target, variant.archiveConfiguration)['PRODUCT_NAME']
    }

    protected Closure<AbstractIOSArtifactsBuilder> builder = {
        checkArgument(variant.mode?.value in IOSBuildMode.values(), "Unknown build mode '${variant.mode?.value}' for variant '$variant.name'")
        [(DEVICE): deviceArtifactsBuilder, (SIMULATOR): simulatorArtifactsBuilder].get(variant.mode.value)
    }.memoize()
}
