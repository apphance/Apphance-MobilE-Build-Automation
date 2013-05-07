package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
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
                new IOSReleaseListener(project, new IOSConfiguration(* [null] * 3), new IOSProjectConfiguration(), executor, iosExecutor))
        this.iosConf = IOSConfigurationRetriever.getIosProjectConfiguration(project)
    }

    void buildAllSimulators() {
        iosConf.targets.each { target ->
            iosSingleVariantBuilder.buildDebugVariant(project, target)
        }
    }
}
