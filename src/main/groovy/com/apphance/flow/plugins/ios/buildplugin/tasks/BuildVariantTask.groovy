package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.plugins.ios.buildplugin.IOSSingleVariantBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class BuildVariantTask extends DefaultTask {

    String group = FLOW_BUILD
    String description = 'Builds single variant for iOS'

    @Inject IOSSingleVariantBuilder builder

    IOSVariant variant

    @TaskAction
    void buildVariant() {
        if (variant != null)
            builder.buildVariant(variant)
        else
            logger.lifecycle('Variant builder not executed - null variant passed')
    }
}
