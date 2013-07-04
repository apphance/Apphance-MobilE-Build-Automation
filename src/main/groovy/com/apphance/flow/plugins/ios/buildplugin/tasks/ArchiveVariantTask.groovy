package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR

class ArchiveVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'archive' action for single variant"

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder

    void build() {
        super.build()
        executor.archiveVariant(variant.tmpDir, variant.archiveCmd)
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
}
