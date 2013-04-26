package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.android.AndroidArtifactBuilder
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class SingleVariantTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject
    AndroidConfiguration androidConfiguration
    @Inject
    AndroidArtifactBuilder artifactBuilder
    AndroidVariantConfiguration variant

    private AndroidSingleVariantJarBuilder androidJarBuilder
    private AndroidSingleVariantApkBuilder androidApkBuilder

    @Inject
    AntExecutor antExecutor

    @Inject
    def init() {
        this.androidApkBuilder = new AndroidSingleVariantApkBuilder(project, antExecutor)
        this.androidJarBuilder = new AndroidSingleVariantJarBuilder(project, antExecutor)
    }

    @TaskAction
    void singleVariant() {
        if (androidConfiguration.isLibrary()) {
            AndroidBuilderInfo bi = artifactBuilder.jarArtifactBuilderInfo(variant)
            androidJarBuilder.buildSingle(bi)
        } else {
            AndroidBuilderInfo bi = artifactBuilder.apkArtifactBuilderInfo(variant)
            androidApkBuilder.buildSingle(bi)
        }
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}