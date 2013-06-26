package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.plugins.android.apphance.tasks.AddApphanceToAndroid
import com.apphance.flow.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static org.gradle.api.logging.Logging.getLogger

/**
 * Adds Apphance in automated way.
 *
 * This is the plugin that links Flow with Apphance service.
 *
 * The plugin provides integration with Apphance service. It performs the
 * following tasks: adding Apphance on-the-fly while building the application
 * (for all Debug builds), removing Apphance on-the-fly while building the application
 * (for all Release builds), submitting the application to apphance at release time.
 */
class AndroidApphancePlugin implements Plugin<Project> {

    def logger = getLogger(this.class)

    @Inject AndroidVariantsConfiguration variantsConf
    @Inject ApphanceConfiguration apphanceConf

    @Override
    void apply(Project project) {
        if (apphanceConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            variantsConf.variants.each { AndroidVariantConfiguration variantConf ->
                if (variantConf.apphanceMode.value != DISABLED) {
                    logger.lifecycle("Adding apphance task for ${variantConf.name}")

                    def buildVariantTask = project.tasks.findByName(variantConf.buildTaskName)
                    buildVariantTask?.doFirst {
                        new AddApphanceToAndroid(variantConf).addApphance()
                    }
                    project.task(variantConf.uploadTaskName, type: UploadAndroidArtifactTask, dependsOn: buildVariantTask?.name).variant = variantConf
                } else {
                    logger.lifecycle("Not adding apphance to ${variantConf.name} because it is not in debug mode")
                }
            }
        }
    }
}
