package com.apphance.ameba.plugins.ios.apphance

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.apphance.tasks.AddIOSApphanceTask
import com.apphance.ameba.plugins.ios.apphance.tasks.UploadIOSArtifactTask
import com.apphance.ameba.plugins.ios.buildplugin.tasks.IOSAllSimulatorsBuilder
import com.apphance.ameba.plugins.release.tasks.ImageMontageTask
import com.apphance.ameba.util.Preconditions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_IMAGE_MONTAGE_TASK_NAME

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 */
@Mixin(Preconditions)
@Mixin(ApphancePluginCommons)
class IOSApphancePlugin implements Plugin<Project> {

    @Inject
    private CommandExecutor executor
    @Inject
    private IOSExecutor iosExecutor

    private Project project
    private IOSProjectConfiguration iosConf

    @Override
    void apply(Project project) {
        this.project = project
        this.iosConf = getIosProjectConfiguration(project)

        addApphanceConfiguration(project)
        preProcessBuildsWithApphance()
        preProcessBuildAllSimulatorsTask()

//        project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
//        project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
//        project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
    }

    private void preProcessBuildsWithApphance() {
        iosConf.allBuildableVariants.each { v ->
            def buildTask = project.tasks["build-${v.id}"]
            buildTask.doFirst { new AddIOSApphanceTask(project, executor, iosExecutor, v).addIOSApphance() }
            prepareSingleBuildUpload(buildTask, v)
        }
    }

    private void prepareSingleBuildUpload(Task buildTask, Expando e) {
        def task = project.task("upload-${e.noSpaceId}")
        task.description = 'Uploads ipa, dsym & image_montage to Apphance server'
        task.group = AMEBA_APPHANCE_SERVICE
        task << { new UploadIOSArtifactTask(project, iosExecutor, e).uploadIOSArtifact() }
        task.dependsOn(buildTask.name)
        task.dependsOn(ImageMontageTask.NAME)
    }

    private void preProcessBuildAllSimulatorsTask() {
        if (project.tasks.findByName(IOSAllSimulatorsBuilder.NAME)) {
            project.tasks[IOSAllSimulatorsBuilder.NAME].doFirst {
                new AddIOSApphanceTask(project, executor, iosExecutor).addIOSApphance()
            }
        }
    }

    static public final String DESCRIPTION =
        "This plugins provides automated adding of Apphance libraries to the project.\n"
}
