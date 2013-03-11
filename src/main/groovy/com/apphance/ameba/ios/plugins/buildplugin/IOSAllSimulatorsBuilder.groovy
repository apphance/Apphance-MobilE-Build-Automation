package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.plugins.release.IOSReleaseListener
import org.gradle.api.Project

/**
 * Task to build iOS simulators - executable images that can be run on OSX.
 */
class IOSAllSimulatorsBuilder {

    private Project project
    private IOSProjectConfiguration iosConf
    private IOSSingleVariantBuilder iosSingleVariantBuilder

    IOSAllSimulatorsBuilder(Project project, CommandExecutor executor) {
        this.project = project
        this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, executor,
                new IOSReleaseListener(project, executor))
        this.iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
    }

    void buildAllSimulators() {
        iosConf.targets.each { target ->
            iosSingleVariantBuilder.buildDebugVariant(project, target)
        }
    }
}
