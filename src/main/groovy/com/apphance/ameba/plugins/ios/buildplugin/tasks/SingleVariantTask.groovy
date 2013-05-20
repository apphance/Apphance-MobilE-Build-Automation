package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class SingleVariantTask extends DefaultTask {

    String group = AMEBA_BUILD
    String description = 'Builds single variant for iOS.'

    @Inject
    IOSSingleVariantBuilder builder
    @Inject
    IOSReleaseListener releaseListener

    AbstractIOSVariant variant

    @TaskAction
    void buildSingleVariant() {
        builder.registerListener(releaseListener)
        builder.buildVariant(variant)
    }
}
