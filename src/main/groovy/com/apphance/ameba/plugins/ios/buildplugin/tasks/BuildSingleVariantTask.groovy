package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class BuildSingleVariantTask extends DefaultTask {

    static final String IOS_CONFIGURATION_LOCAL_PROPERTY = 'ios.configuration'
    static final String IOS_TARGET_LOCAL_PROPERTY = 'ios.target'


    static final NAME = 'buildSingleVariant'
    String group = AMEBA_BUILD
    String description = 'Builds single variant for iOS. Requires ios.target and ios.configuration properties'

    @Inject
    IOSExecutor iosExecutor

    @TaskAction
    void buildSingleVariant() {
        def singleVariantBuilder = new IOSSingleVariantBuilder(project, iosExecutor)
        String target = PropertyCategory.readExpectedProperty(IOS_TARGET_LOCAL_PROPERTY)
        String configuration = PropertyCategory.readExpectedProperty(IOS_CONFIGURATION_LOCAL_PROPERTY)
        singleVariantBuilder.buildNormalVariant(project, target, configuration)
    }
}
