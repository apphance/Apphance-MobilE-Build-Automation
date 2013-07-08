package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class BuildVariantTask extends DefaultTask {

    String group = FLOW_BUILD
    String description = "Executes 'build' action for single variant"

    @Inject IOSExecutor executor

    IOSVariant variant

    @TaskAction
    void build() {
        Preconditions.checkNotNull(variant, 'Null variant passed to builder!')
        executor.buildVariant(variant.tmpDir, variant.buildCmd)
    }
}
