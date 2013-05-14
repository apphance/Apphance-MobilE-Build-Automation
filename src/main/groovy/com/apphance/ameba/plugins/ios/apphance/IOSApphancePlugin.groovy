package com.apphance.ameba.plugins.ios.apphance

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import com.apphance.ameba.plugins.ios.apphance.tasks.AddIOSApphanceTask
import com.apphance.ameba.plugins.ios.apphance.tasks.UploadIOSArtifactTask
import com.apphance.ameba.plugins.ios.buildplugin.tasks.IOSAllSimulatorsBuilder
import com.apphance.ameba.plugins.release.tasks.ImageMontageTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.configuration.apphance.ApphanceMode.*

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 * This plugins provides automated adding of Apphance libraries to the project
 */
@Mixin(ApphancePluginCommons)
class IOSApphancePlugin implements Plugin<Project> {

    @Inject
    CommandExecutor executor
    @Inject
    IOSExecutor iosExecutor
    @Inject
    IOSVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {
        addApphanceConfiguration(project)

        variantsConf.variants.each { AbstractIOSVariant variant ->
            if (variant.apphanceMode.value in [QA, PROD, SILENT]*.toString()) {
                def addApphance = { new AddIOSApphanceTask(it).addIOSApphance() }

                project.tasks[variant.buildTaskName()].doFirst(addApphance)
                project.tasks[IOSAllSimulatorsBuilder.NAME]?.doFirst(addApphance)

                project.task("upload${variant.name}",
                        type: UploadIOSArtifactTask,
                        dependsOn: [variant.buildTaskName(), ImageMontageTask.NAME]).variant = variant
            }
        }
    }
}
