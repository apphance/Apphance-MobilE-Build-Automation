package com.apphance.ameba.plugins.ios.release

import org.gradle.api.Project

/**
 * Retrieves iOS release.
 *
 */
class IOSReleaseConfigurationRetriever {

    public static final String IOS_RELEASE_CONFIGURATION_KEY = 'ios.release.configuration'

    public static IOSReleaseConfigurationOLD getIosReleaseConfiguration(Project project) {
        if (!project.ext.has(IOS_RELEASE_CONFIGURATION_KEY)) {
            project.ext.set(IOS_RELEASE_CONFIGURATION_KEY, new IOSReleaseConfigurationOLD())
        }
        return project.ext.get(IOS_RELEASE_CONFIGURATION_KEY)
    }
}
