package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSDeviceArtifactsBuilder
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.google.common.base.Preconditions.checkArgument

class DeviceVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'archive' action for single device variant"

    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject XCSchemeParser schemeParser

    final IOSBuildMode validationMode = DEVICE

    @Override
    void build() {
        validate()

        logger.info("Adding post archive action to scheme file: $variant.schemeFile.absolutePath")
        schemeParser.addPostArchiveAction(variant.schemeFile)
        def archiveOutput = iosExecutor.buildVariant(variant.tmpDir, cmd)

        if (releaseConf.isEnabled()) {
            def info = artifactProvider.deviceInfo(variant)
            info.archiveDir = findArchiveFile(archiveOutput)
            validateArchiveFile(info.archiveDir)
            info.appName = appName
            info.productName = productName

            deviceArtifactsBuilder.buildArtifacts(info)
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
        file
    }

    @PackageScope
    validateArchiveFile(File archive) {
        checkArgument(archive?.exists() && archive?.isDirectory(), "Xcarchive file: ${archive?.absolutePath} does not exist or is not a directory")
    }
}
