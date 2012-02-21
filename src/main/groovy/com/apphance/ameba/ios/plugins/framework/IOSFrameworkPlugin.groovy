package com.apphance.ameba.ios.plugins.framework;



import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin;



/**
 * Plugin for preparing reports after successful build.
 *
 */
class IOSFrameworkPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(IOSFrameworkPlugin.class)

    def void apply (Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        project.task('buildFramework', type: IOSBuildFrameworkTask)
        project.task('verifyFrameworkSetup', type: VerifyFrameworkSetupTask.class)
        project.task('prepareFrameworkSetup', type: PrepareFrameworkSetupTask.class)
        project.task('showFrameworkSetup', type: ShowFrameworkSetupTask.class)
    }
}
