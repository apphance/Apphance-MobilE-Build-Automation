package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class SingleVariantTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject
    private AndroidConfiguration androidConfiguration
    AndroidVariantConfiguration variant

    private AndroidSingleVariantJarBuilder androidJarBuilder
    private AndroidSingleVariantApkBuilder androidApkBuilder

    @Inject
    def init() {
        this.androidApkBuilder = new AndroidSingleVariantApkBuilder(project, androidConfiguration)
        this.androidJarBuilder = new AndroidSingleVariantJarBuilder(project, androidConfiguration)
    }

    @TaskAction
    void singleVariant() {
        if (androidConfiguration.isLibrary()) {
            AndroidBuilderInfo bi = androidJarBuilder.buildJarArtifactBuilderInfo(variant)
            androidJarBuilder.buildSingle(bi)
        } else {
            AndroidBuilderInfo bi = androidApkBuilder.buildApkArtifactBuilderInfo(variant)
            androidApkBuilder.buildSingle(bi)
        }
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}