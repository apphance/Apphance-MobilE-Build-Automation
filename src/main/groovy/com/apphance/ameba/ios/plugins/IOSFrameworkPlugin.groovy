package com.apphance.ameba.ios.plugins;



import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ios.IOSBuildFrameworkTask;

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
