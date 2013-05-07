package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.configuration.ios.IOSFrameworkConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.ameba.plugins.ios.framework.tasks.BuildFrameworkTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 * Plugin for preparing reports after successful IOS build.
 *
 * This plugins provides functionality of building shared framework for IOS projects.
 *
 * While iOS itself provides a number of frameworks (shared libraries) that
 * can be used in various projects. It is undocumented feature of iOS that one can create own
 * framework. This plugin closes the gap.
 */
class IOSFrameworkPlugin implements Plugin<Project> {

    @Inject
    IOSFrameworkConfiguration iosFrameworkConf

    @Override
    void apply(Project project) {
        if (iosFrameworkConf.isEnabled()) {
            project.task(BuildFrameworkTask.NAME,
                    type: BuildFrameworkTask,
                    dependsOn: [CopyMobileProvisionTask.NAME])
        }
    }
}
