package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.plugins.release.IOSReleaseListener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_BUILD

/**
 * Task to build iOS simulators - executable images that can be run on OSX.
 */
class IOSBuildAllSimulatorsTask extends DefaultTask {

    IOSProjectConfiguration iosConf
    IOSSingleVariantBuilder iosSingleVariantBuilder

    IOSBuildAllSimulatorsTask() {
        this.group = AMEBA_BUILD
        this.description = 'Builds all simulators for the project'
        this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, new IOSReleaseListener(project))
        this.dependsOn(project.readProjectConfiguration, project.copyMobileProvision, project.copyDebugSources)
    }

    @TaskAction
    void buildAllSimulators() {
        iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
        iosConf.targets.each { target ->
            iosSingleVariantBuilder.buildDebugVariant(project, target)
        }
    }
}
