package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

abstract class AbstractBuildVariantTask extends DefaultTask {

    String group = FLOW_BUILD

    @Inject IOSConfiguration conf
    @Inject IOSExecutor iosExecutor

    IOSVariant variant

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
}