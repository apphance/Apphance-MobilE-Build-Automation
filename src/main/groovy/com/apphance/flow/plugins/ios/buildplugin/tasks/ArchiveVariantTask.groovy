package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder
import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class ArchiveVariantTask extends DefaultTask {

    String description = "Executes 'archive' action for single variant"
    String group = FLOW_BUILD

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject IOSDeviceArtifactsBuilder deviceArtifactsBuilder
    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder
    @Inject IOSExecutor executor

    IOSVariant variant

    @TaskAction
    void build() {
        Preconditions.checkNotNull(variant, 'Null variant passed to builder!')
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
