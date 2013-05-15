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

    //TODO sorry for 'TODOS' but have no time to correct it, and will forget later :( - Opal
    //TODO remove unused injected fields (executors)
    //TODO inject apphance conf and add tasks and initialize plugin if this conf is enabled
    //TODO write a spec for this class that will be checking if tasks are added or not (apphance conf - enabled/disabled)
    //TODO com.apphance.ameba.plugins.ios.apphance.tasks.AddIOSApphanceTask class has injected fields, they won't be initialized when 'new' operator is used
    //TODO minor: variant.apphanceMode.value in [QA, PROD, SILENT]*.toString() == variant.apphanceMode.value != DISABLED

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
