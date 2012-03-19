package com.apphance.ameba.android;

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty
import com.sun.org.apache.xpath.internal.XPathAPI



public class AndroidProjectConfigurationRetriever {
    static Logger logger = Logging.getLogger(AndroidProjectConfigurationRetriever.class)
    public static final String ANDROID_PROJECT_CONFIGURATION_KEY = 'android.project.configuration'
    static AndroidManifestHelper androidManifestHelper = new AndroidManifestHelper()
    static AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()

    static AndroidProjectConfiguration getAndroidProjectConfiguration(final Project project){
        if (!project.hasProperty(ANDROID_PROJECT_CONFIGURATION_KEY)) {
            project.ext[ANDROID_PROJECT_CONFIGURATION_KEY] = new AndroidProjectConfiguration()
        }
        return project.ext[ANDROID_PROJECT_CONFIGURATION_KEY]
    }

    static void readAndroidProjectConfiguration(Project project) {
        use (PropertyCategory) {
            AndroidProjectConfiguration androidConf = getAndroidProjectConfiguration(project)
            androidConf.mainVariant = project.readProperty(AndroidProjectProperty.MAIN_VARIANT)
            if (androidConf.mainVariant == null || androidConf.mainVariant.empty) {
                androidConf.mainVariant = androidConf.variants[0]
            }
            def mainProjectManifest = androidManifestHelper.getParsedManifest(project.rootDir)
            androidConf.mainProjectPackage = XPathAPI.selectSingleNode(mainProjectManifest, "/manifest/@package").nodeValue
            androidConf.mainProjectName = buildXmlHelper.readProjectName(project.rootDir)
        }
    }
}
