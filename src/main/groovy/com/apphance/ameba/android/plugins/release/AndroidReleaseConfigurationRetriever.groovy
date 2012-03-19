package com.apphance.ameba.android.plugins.release

import org.gradle.api.Project;

import com.apphance.ameba.ProjectConfiguration;

class AndroidReleaseConfigurationRetriever {

    public static final String ANDROID_RELEASE_CONFIGURATION_KEY = 'android.release.configuration'

    public static AndroidReleaseConfiguration getAndroidReleaseConfiguration(Project project) {
        if (!project.hasProperty(ANDROID_RELEASE_CONFIGURATION_KEY)) {
            project.ext[ANDROID_RELEASE_CONFIGURATION_KEY] = new AndroidReleaseConfiguration()
        }
        return project.ext[ANDROID_RELEASE_CONFIGURATION_KEY]
    }
}
