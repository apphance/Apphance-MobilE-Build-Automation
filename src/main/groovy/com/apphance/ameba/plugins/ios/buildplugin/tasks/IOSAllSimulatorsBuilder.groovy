package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

/**
 * Task to build iOS simulators - executable images that can be run on OSX.
 */
class IOSAllSimulatorsBuilder extends DefaultTask {

    static final NAME = 'buildAllSimulators'
    String group = AMEBA_BUILD
    String description = 'Builds all simulators for the project'

    @Inject
    IOSConfiguration conf
    @Inject
    IOSExecutor iosExecutor
    @Inject
    IOSReleaseListener releaseListener
    @Inject
    IOSSingleVariantBuilder builder

    @TaskAction
    void buildAllSimulators() {
        builder.registerListener(releaseListener)
        conf.targets.each { target ->
            builder.buildDebugVariant(target)
        }
    }
}
