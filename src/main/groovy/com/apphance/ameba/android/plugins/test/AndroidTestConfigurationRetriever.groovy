package com.apphance.ameba.android.plugins.test

import org.gradle.api.Project

class AndroidTestConfigurationRetriever {

    public static final String ANDROID_TEST_CONFIGURATION_KEY = 'android.test.configuration'


    static AndroidTestConfiguration getAndroidTestConfiguration(Project project) {
        if (!project.ext.has(ANDROID_TEST_CONFIGURATION_KEY)) {
            project.ext.set(ANDROID_TEST_CONFIGURATION_KEY, new AndroidTestConfiguration())
        }
        return project.ext.get(ANDROID_TEST_CONFIGURATION_KEY)
    }
}
