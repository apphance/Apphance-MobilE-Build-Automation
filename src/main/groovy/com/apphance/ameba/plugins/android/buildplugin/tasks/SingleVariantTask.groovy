package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

class SingleVariantTask extends DefaultTask {

    String group = AMEBA_BUILD
    AndroidVariantConfiguration variant
    @Inject AndroidConfiguration androidConfiguration

    private AndroidSingleVariantJarBuilder androidJarBuilder
    private AndroidSingleVariantApkBuilder androidApkBuilder

    @Inject
    def init() {
        AndroidProjectConfiguration androidConf = getAndroidProjectConfiguration(project)
        this.androidApkBuilder = new AndroidSingleVariantApkBuilder(project, androidConf)
        this.androidJarBuilder = new AndroidSingleVariantJarBuilder(project, androidConf)
    }

    @TaskAction
    void singleVariant() {
        String debugRelease = variant.mode.value.toLowerCase().capitalize()
        if (androidConfiguration.isLibrary()) {
            AndroidBuilderInfo bi = androidJarBuilder.buildJarArtifactBuilderInfo(variant.name, debugRelease)
            androidJarBuilder.buildSingle(bi)
        } else {
            AndroidBuilderInfo bi = androidApkBuilder.buildApkArtifactBuilderInfo(variant.name, debugRelease)
            androidApkBuilder.buildSingle(bi)
        }
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}