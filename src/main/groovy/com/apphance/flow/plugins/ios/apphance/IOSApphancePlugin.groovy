package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.apphance.ApphancePluginCommons
import com.apphance.flow.plugins.ios.apphance.tasks.IOSApphanceEnhancerFactory
import com.apphance.flow.plugins.ios.apphance.tasks.UploadIOSArtifactTask
import com.apphance.flow.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.flow.plugins.ios.release.IOSReleaseListener
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 * This plugins provides automated adding of Apphance libraries to the project
 */
@Mixin(ApphancePluginCommons)
class IOSApphancePlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSApphanceEnhancerFactory addIOSApphanceTaskFactory
    @Inject IOSSingleVariantBuilder builder
    @Inject IOSReleaseListener listener

    @Override
    void apply(Project project) {
        if (apphanceConf.enabled) {

            logger.lifecycle("Applying plugin ${getClass().simpleName}")

            addApphanceConfiguration(project)

            variantsConf.variants.each { variant ->

                if (variant.apphanceMode.value in [QA, PROD, SILENT]) {
                    logger.info("Adding apphance for variant '$variant.name'")

                    def addApphance = { addIOSApphanceTaskFactory.create(variant).addApphance() }

                    project.tasks[variant.buildTaskName].doFirst(addApphance)

                    def uploadTask =
                        project.task("upload$variant.name",
                                type: UploadIOSArtifactTask,
                                dependsOn: [variant.buildTaskName, ImageMontageTask.NAME]) as UploadIOSArtifactTask
                    uploadTask.variant = variant
                } else {
                    logger.info("Apphance is disabled for variant '$variant.name'")
                }
            }

            builder.registerListener(listener)
        }
    }
}
