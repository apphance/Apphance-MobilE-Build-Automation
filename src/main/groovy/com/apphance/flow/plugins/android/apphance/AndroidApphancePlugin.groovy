package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.plugins.android.apphance.tasks.AddApphanceToAndroid
import com.apphance.flow.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static org.gradle.api.logging.Logging.getLogger

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
                    def addApphanceTask = project.task("addApphance$variantConf.name")
                    addApphanceTask.doFirst {
                        new AddApphanceToAndroid(variantConf, apphanceConf.enableShaking.value).addApphance()
                    }
                    buildVariantTask.dependsOn addApphanceTask
                    addApphanceTask.dependsOn project.tasks.getByName(VerifySetupTask.NAME)
                    project.task(variantConf.uploadTaskName, type: UploadAndroidArtifactTask, dependsOn: buildVariantTask?.name).variant = variantConf
                } else {
                    logger.lifecycle("Not adding apphance to ${variantConf.name} because it is $DISABLED")
                }
            }
        }
    }
}
