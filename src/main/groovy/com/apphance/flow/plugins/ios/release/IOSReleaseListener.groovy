package com.apphance.flow.plugins.ios.release

import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.buildplugin.IOSBuildListener
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static org.gradle.api.logging.Logging.getLogger

/**
 * Build listener for releases.
 *
 */
@Singleton
class IOSReleaseListener implements IOSBuildListener {

    private logger = getLogger(getClass())

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
                logger.warn("Unrecognized mode: $bi.mode, builder info: $bi")
        }
    }
}
