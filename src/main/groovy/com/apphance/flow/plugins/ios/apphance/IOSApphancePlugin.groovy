package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.apphance.ApphancePluginCommons
import com.apphance.flow.plugins.ios.apphance.tasks.AddIOSApphanceTaskFactory
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

    def log = getLogger(this.class)

    @Inject IOSVariantsConfiguration variantsConf
    @Inject ApphanceConfiguration apphanceConf
    @Inject AddIOSApphanceTaskFactory addIOSApphanceTaskFactory
    @Inject IOSSingleVariantBuilder builder
    @Inject IOSReleaseListener listener

    @Override
    void apply(Project project) {
        if (apphanceConf.enabled) {
            addApphanceConfiguration(project)

            variantsConf.variants.each { AbstractIOSVariant variant ->

                if (variant.apphanceMode.value in [QA, PROD, SILENT]) {
                    def addApphance = { addIOSApphanceTaskFactory.create(variant).addIOSApphance() }

                    project.tasks[variant.getBuildTaskName()].doFirst(addApphance)

                    project.task("upload${variant.name}",
                            type: UploadIOSArtifactTask,
                            dependsOn: [variant.getBuildTaskName(), ImageMontageTask.NAME]).variant = variant
                } else {
                    log.lifecycle("Apphance is disabled for variant '${variant.name}'")
                }
            }

            builder.registerListener(listener)
        }
    }
}