package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import org.gradle.api.Project

class IOSConfigurationRetriever {
    public static final String IOS_PROJECT_CONFIGURATION = 'ios.project.configuration'

    static IOSProjectConfiguration getIosProjectConfiguration(Project project) {
        if (!project.ext.has(IOS_PROJECT_CONFIGURATION)) {
            project.ext.set(IOS_PROJECT_CONFIGURATION, new IOSProjectConfiguration())
        }
        return project.ext.get(IOS_PROJECT_CONFIGURATION)
    }
}
