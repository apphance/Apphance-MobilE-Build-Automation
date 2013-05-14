package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.plugins.android.AndroidArtifactProvider
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class SingleVariantTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidArtifactProvider artifactBuilder
    @Inject
    AndroidSingleVariantJarBuilder jarBuilder
    @Inject
    AndroidSingleVariantApkBuilder apkBuilder

    AndroidVariantConfiguration variant

    @TaskAction
    void singleVariant() {
        conf.isLibrary() ?
            jarBuilder.buildSingle(artifactBuilder.jarBuilderInfo(variant)) :
            apkBuilder.buildSingle(artifactBuilder.apkBuilderInfo(variant))
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}