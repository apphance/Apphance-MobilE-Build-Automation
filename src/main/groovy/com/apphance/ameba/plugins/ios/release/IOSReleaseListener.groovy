package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.plugins.ios.builder.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.buildplugin.IOSBuildListener

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.ameba.configuration.ios.IOSBuildMode.SIMULATOR
import static org.gradle.api.logging.Logging.getLogger

/**
 * Build listener for releases.
 *
 */
@com.google.inject.Singleton
class IOSReleaseListener implements IOSBuildListener {

    def l = getLogger(getClass())

    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder

    @Override
    void buildDone(IOSBuilderInfo bi) {

        switch (bi.mode) {
            case DEVICE:
                deviceArtifactsBuilder.buildArtifacts(bi)
                break

            case SIMULATOR:
                simulatorArtifactsBuilder.buildArtifacts(bi)
                break

            default:
                l.info("Unrecognized mode: ${bi.mode}, builder info: $bi")
        }
    }
}
