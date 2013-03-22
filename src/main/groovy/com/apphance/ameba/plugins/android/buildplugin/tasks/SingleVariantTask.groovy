package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidEnvironment
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import org.gradle.api.Project

import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

class SingleVariantTask {

    private AndroidEnvironment androidEnvironment
    private AndroidSingleVariantJarBuilder androidJarBuilder
    private AndroidSingleVariantApkBuilder androidApkBuilder

    SingleVariantTask(Project project, AndroidEnvironment androidEnvironment) {
        this.androidEnvironment = androidEnvironment
        AndroidProjectConfiguration androidConf = getAndroidProjectConfiguration(project)
        this.androidApkBuilder = new AndroidSingleVariantApkBuilder(project, androidConf)
        this.androidJarBuilder = new AndroidSingleVariantJarBuilder(project, androidConf)
    }

    void singleVariant(String variant, String debugRelease) {
        if (androidEnvironment.isLibrary()) {
            AndroidBuilderInfo bi = androidJarBuilder.buildJarArtifactBuilderInfo(variant, debugRelease)
            androidJarBuilder.buildSingle(bi)
        } else {
            AndroidBuilderInfo bi = androidApkBuilder.buildApkArtifactBuilderInfo(variant, debugRelease)
            androidApkBuilder.buildSingle(bi)
        }
    }
}