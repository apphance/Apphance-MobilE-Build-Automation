package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.google.common.base.Preconditions.checkArgument

class ArchiveVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'archive' action for single variant"

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder
    @Inject XCSchemeParser schemeParser

    void build() {
        super.build()
        schemeParser.addPostArchiveAction(variant.schemeFile)
        def output = executor.archiveVariant(variant.tmpDir, variant.archiveCmd)
        def archiveFile = findArchiveFile(output)

        logger.info("Archive file found: ${archiveFile?.absolutePath}")
        checkArgument(archiveFile != null && archiveFile.exists(), "Impossible to find archive file: ${archiveFile?.absolutePath}, for variant: $variant.name")

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
    }

    private File findArchiveFile(Iterator<String> compilerOutput) {
        def line = compilerOutput.find { it.contains('FLOW_ARCHIVE_PATH') }
        line ? new File(line.split('=')[1].trim()) : null
    }
}
