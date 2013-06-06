package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.api.logging.Logging.getLogger

class SingleVariantTask extends DefaultTask {

    private l = getLogger(getClass())

    String group = FLOW_BUILD
    String description = 'Builds single variant for iOS.'

    @Inject IOSSingleVariantBuilder builder

    AbstractIOSVariant variant

    @TaskAction
    void buildSingleVariant() {
        if (variant != null)
            builder.buildVariant(variant)
        else
            l.lifecycle('Variant builder not executed - null variant passed')
    }
}
