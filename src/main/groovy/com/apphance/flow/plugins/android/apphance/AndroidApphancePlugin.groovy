package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.plugins.android.apphance.tasks.AddApphanceToAndroid
import com.apphance.flow.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.flow.plugins.android.buildplugin.tasks.CopySourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for enabling Apphance (<a href="http://www.apphance.com">apphance.com</a>) library in android project.<br/>
 *
 * The plugin provides integration with Apphance service. It performs the
 * following tasks: adding Apphance on-the-fly while building the application for configured variants,
 * submitting the application to apphance at release time.<br/>
 * Adding Apphance is implemented according to library installation documentation:
 * <a href="http://help.apphance.com/library-installation/android">help.apphance.com</a>
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
                    def addApphanceTask = project.task("addApphance$variantConf.name", description: "adding Apphance on-the-fly to $variantConf.name variant")
                    addApphanceTask.doFirst {
                        new AddApphanceToAndroid(variantConf, apphanceConf.enableShaking.value).addApphance()
                    }
                    addApphanceTask.dependsOn CopySourcesTask.NAME
                    buildVariantTask.dependsOn addApphanceTask
                    project.task(variantConf.uploadTaskName, type: UploadAndroidArtifactTask, dependsOn: buildVariantTask?.name).variant = variantConf
                } else {
                    logger.lifecycle("Not adding apphance to ${variantConf.name} because it is $DISABLED")
                }
            }
        }
    }
}
