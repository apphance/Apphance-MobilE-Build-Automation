package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.plugins.android.builder.AndroidArtifactProvider
import com.apphance.ameba.plugins.android.builder.AndroidSingleVariantBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class SingleVariantTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidArtifactProvider artifactBuilder
    @Inject AndroidSingleVariantBuilder builder

    AndroidVariantConfiguration variant

    @TaskAction
    void singleVariant() {
        builder.buildSingle(artifactBuilder.builderInfo(variant))
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}