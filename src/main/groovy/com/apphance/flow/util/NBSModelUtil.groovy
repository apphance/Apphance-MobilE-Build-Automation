package com.apphance.flow.util

import com.android.build.gradle.AppExtension
import com.android.builder.DefaultManifestParser
import com.android.builder.ManifestParser
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.Project

class NBSModelUtil {

    static AppExtension getAndroidNBS(Project project) {
        project.hasProperty('android') && project.android instanceof AppExtension ? project.android as AppExtension : null
    }

    static String getVersionCode(Project project) {
        def androidNBS = getAndroidNBS(project)

        if (androidNBS) {
            def helper = new AndroidManifestHelper()
            def versions = helper.readVersion(androidNBS.sourceSets.main.manifest.srcFile.parentFile)
            def code = androidNBS.defaultConfig.versionCode
            return code > 0 ? code : versions.versionCode
        }
        ''
    }

    static String getVersionName(Project project) {
        def androidNBS = getAndroidNBS(project)

        if (androidNBS) {
            ManifestParser manifestParser = new DefaultManifestParser()
            return androidNBS.defaultConfig.versionName ?: manifestParser.getVersionName(androidNBS.sourceSets.main.manifest.srcFile)
        }
        ''
    }
}
