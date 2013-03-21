package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import org.gradle.api.Project

/**
 * Task to build iOS simulators - executable images that can be run on OSX.
 */
class IOSAllSimulatorsBuilder {

    private Project project
    private IOSProjectConfiguration iosConf
    private IOSSingleVariantBuilder iosSingleVariantBuilder

    IOSAllSimulatorsBuilder(Project project, CommandExecutor executor, IOSExecutor iosExecutor) {
        this.project = project
        this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, iosExecutor,
                new IOSReleaseListener(project, executor, iosExecutor))
        this.iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
    }

    void buildAllSimulators() {
        iosConf.targets.each { target ->
            iosSingleVariantBuilder.buildDebugVariant(project, target)
        }
    }
}
