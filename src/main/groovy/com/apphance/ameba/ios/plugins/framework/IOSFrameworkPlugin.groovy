package com.apphance.ameba.ios.plugins.framework;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin;

/**
 * Plugin for preparing reports after successful IOS build.
 *
 */
class IOSFrameworkPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(IOSFrameworkPlugin.class)

    def void apply (Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        project.task('buildFramework', type: IOSBuildFrameworkTask)
        project.prepareSetup.prepareSetupOperations << new PrepareFrameworkSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyFrameworkSetupOperation()
        project.showSetup.showSetupOperations << new ShowFrameworkSetupOperation()
    }

    static public final String DESCRIPTION =
"""This plugins provides functionality of building shared framework for IOS projects.

While iOS itself provides a number of frameworks (shared libraries) that
can be used in various projects. It is undocumented feature of iOS that one can create own
framework. This plugin closes the gap.
"""


}
