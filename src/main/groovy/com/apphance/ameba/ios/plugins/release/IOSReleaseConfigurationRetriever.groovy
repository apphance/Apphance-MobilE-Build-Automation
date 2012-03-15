package com.apphance.ameba.ios.plugins.release

import org.gradle.api.Project;

import com.apphance.ameba.ProjectConfiguration;

class IOSReleaseConfigurationRetriever {

    public static final String IOS_RELEASE_CONFIGURATION_KEY = 'ios.release.configuration'

    public static IOSReleaseConfiguration getIosReleaseConfiguration(Project project) {
        if (!project.hasProperty(IOS_RELEASE_CONFIGURATION_KEY)) {
            project[IOS_RELEASE_CONFIGURATION_KEY] = new IOSReleaseConfiguration()
        }
        return project[IOS_RELEASE_CONFIGURATION_KEY]
    }
}
