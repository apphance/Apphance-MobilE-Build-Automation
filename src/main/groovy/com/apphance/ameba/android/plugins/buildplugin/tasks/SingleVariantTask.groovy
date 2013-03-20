package com.apphance.ameba.android.plugins.buildplugin.tasks

import com.apphance.ameba.android.*
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Project

import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

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