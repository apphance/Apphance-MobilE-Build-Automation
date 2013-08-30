package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.artifact.builder.AbstractIOSArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSSimulatorArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.info.IOSSimArtifactInfo
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
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
        checkArgument(variant.mode.value != FRAMEWORK, "Invalid build mode: $FRAMEWORK!")

        logger.info("Adding post archive action to scheme file: $variant.schemeFile.absolutePath")
        schemeParser.addPostArchiveAction(variant.schemeFile)
        def archiveOutput = iosExecutor.buildVariant(variant.tmpDir, cmd)

        if (releaseConf.enabled) {
            def info = infoProvider.call()
            info.archiveDir = findArchiveFile(archiveOutput)
            info.appName = appName()
            info.productName = productName()

            builder.call().buildArtifacts(info)
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
        checkArgument(file?.exists() && file?.isDirectory(), "Xcarchive file: ${file?.absolutePath} does not exist or is not a directory")
        file
    }

    private String appName() {
        iosExecutor.buildSettings(variant.target, variant.archiveConfiguration)['FULL_PRODUCT_NAME']
    }

    private String productName() {
        iosExecutor.buildSettings(variant.target, variant.archiveConfiguration)['PRODUCT_NAME']
    }

    protected Closure<? extends AbstractIOSArtifactsBuilder> builder = {
        [(DEVICE): deviceArtifactsBuilder, (SIMULATOR): simulatorArtifactsBuilder].get(variant.mode.value)
    }.memoize()

    protected Closure<? extends IOSSimArtifactInfo> infoProvider = {
        [(DEVICE): artifactProvider.deviceInfo(variant), (SIMULATOR): artifactProvider.simInfo(variant)].get(variant.mode.value)
    }.memoize()
}
