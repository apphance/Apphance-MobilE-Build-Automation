package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.util.FlowUtils
import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

abstract class AbstractBuildVariantTask extends DefaultTask {

    String group = FLOW_BUILD

    @Inject IOSConfiguration conf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSExecutor iosExecutor
    @Inject IOSArtifactProvider artifactProvider
    @Inject FlowUtils fu

    AbstractIOSVariant variant

    @TaskAction
    abstract void build()

    protected List<String> getArchCmd() {
        variant.mode.value == SIMULATOR ? ['-arch', 'i386'] : []
    }

    protected List<String> getSdkCmd() {
        switch (variant.mode.value) {
            case SIMULATOR:
                conf.simulatorSdk.value ? ['-sdk', conf.simulatorSdk.value] : []
                break
            case DEVICE:
                conf.sdk.value ? ['-sdk', conf.sdk.value] : []
                break
            default:
                []
        }
    }

    protected validate() {
        Preconditions.checkNotNull(variant, 'Null variant passed to builder!')
        Preconditions.checkArgument(variant.mode.value == validationMode, "Invalid build mode: $variant.mode.value!")
    }

    @Lazy
    protected String productName = {
        iosExecutor.buildSettings(variant.target, variant.archiveConfiguration)['PRODUCT_NAME']
    }()

    @Lazy
    protected String appName = {
        iosExecutor.buildSettings(variant.target, variant.archiveConfiguration)['FULL_PRODUCT_NAME']
    }()

    abstract protected IOSBuildMode getValidationMode()
}
