package com.apphance.ameba.plugins.android

import com.apphance.ameba.PropertyCategory
import com.google.inject.Inject
import org.gradle.api.Project

/**
 * Retrieves android project configuration.
 *
 */
public class AndroidProjectConfigurationRetriever {
    public static final String ANDROID_PROJECT_CONFIGURATION_KEY = 'android.project.configuration'

    def static manifestHelper = new AndroidManifestHelper()
    def static buildXmlHelper = new AndroidBuildXmlHelper()

    static AndroidProjectConfiguration getAndroidProjectConfiguration(final Project project) {
        if (!project.ext.has(ANDROID_PROJECT_CONFIGURATION_KEY)) {
            project.ext.set(ANDROID_PROJECT_CONFIGURATION_KEY, new AndroidProjectConfiguration())
        }
        return project.ext.get(ANDROID_PROJECT_CONFIGURATION_KEY)
    }

    static void readAndroidProjectConfiguration(Project project) {
        use(PropertyCategory) {
            AndroidProjectConfiguration androidConf = getAndroidProjectConfiguration(project)
            androidConf.mainProjectPackage = manifestHelper.androidPackage(project.rootDir)
            androidConf.mainProjectName = buildXmlHelper.projectName(project.rootDir)
        }
    }
}
