package com.apphance.ameba.android.plugins.release

import org.gradle.api.Project

/**
 * Retrieves release configuration.
 *
 */
class AndroidReleaseConfigurationRetriever {

    public static final String ANDROID_RELEASE_CONFIGURATION_KEY = 'android.release.configuration'

    public static AndroidReleaseConfiguration getAndroidReleaseConfiguration(Project project) {
        if (!project.ext.has(ANDROID_RELEASE_CONFIGURATION_KEY)) {
            project.ext.set(ANDROID_RELEASE_CONFIGURATION_KEY, new AndroidReleaseConfiguration())
        }
        return project.ext.get(ANDROID_RELEASE_CONFIGURATION_KEY)
    }
}
