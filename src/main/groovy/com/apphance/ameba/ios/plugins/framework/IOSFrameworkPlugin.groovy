package com.apphance.ameba.ios.plugins.framework;



import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * Plugin for preparing reports after successful build.
 *
 */
class IOSFrameworkPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(IOSFrameworkPlugin.class)

    def void apply (Project project) {
        project.task('buildFramework', type: IOSBuildFrameworkTask)
    }
}
