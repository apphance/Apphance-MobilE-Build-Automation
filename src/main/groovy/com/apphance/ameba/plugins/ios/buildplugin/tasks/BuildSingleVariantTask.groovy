package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import org.gradle.api.Project

import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.IOS_CONFIGURATION_LOCAL_PROPERTY
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.IOS_TARGET_LOCAL_PROPERTY

class BuildSingleVariantTask {

    private Project project
    private IOSExecutor iosExecutor

    BuildSingleVariantTask(Project project, IOSExecutor iosExecutor) {
        this.project = project
        this.iosExecutor = iosExecutor
    }

    void buildSingleVariant() {
        use(PropertyCategory) {
            def singleVariantBuilder = new IOSSingleVariantBuilder(project, iosExecutor)
            String target = project.readExpectedProperty(IOS_TARGET_LOCAL_PROPERTY)
            String configuration = project.readExpectedProperty(IOS_CONFIGURATION_LOCAL_PROPERTY)
            singleVariantBuilder.buildNormalVariant(project, target, configuration)
        }
    }
}
