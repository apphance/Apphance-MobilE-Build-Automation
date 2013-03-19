package com.apphance.ameba.android.plugins.apphance

import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.plugins.apphance.tasks.AddAndroidApphanceTask
import com.apphance.ameba.android.plugins.apphance.tasks.AndroidLogsConversionTask
import com.apphance.ameba.android.plugins.apphance.tasks.ApphanceLogsConversionTask
import com.apphance.ameba.android.plugins.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.ameba.apphance.ApphancePluginCommons
import com.apphance.ameba.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_IMAGE_MONTAGE_TASK_NAME

/**
 * Adds Apphance in automated way.
 *
 */
@Mixin(ApphancePluginCommons)
class AndroidApphancePlugin implements Plugin<Project> {

    @Inject
    private CommandExecutor executor

    private Project project
    private AndroidProjectConfiguration androidConf

    public static final String CONVERT_LOGS_TO_APPHANCE_TASK_NAME = 'convertLogsToApphance'
    public static final String CONVERT_LOGS_TO_ANDROID_TASK_NAME = 'convertLogsToAndroid'

    @Override
    void apply(Project project) {
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)

        addApphanceConfiguration(project)

        preProcessBuildsWithApphance()

        prepareConvertLogsToApphanceTask()
        prepareConvertLogsToAndroidTask()

        //TODO what do to with these operations?
        project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
        project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
    }

    private void preProcessBuildsWithApphance() {
        androidConf.buildableVariants.each { variant ->
            if (androidConf.debugRelease.get(variant) == 'Debug') {
                def task = project.tasks["buildDebug-$variant"]
                task.doFirst {
                    new AddAndroidApphanceTask(project).addApphance(variant)
                }
                prepareSingleBuildUploadTask(variant, task.name)
            }
        }
    }

    private void prepareConvertLogsToApphanceTask() {
        def task = project.task(CONVERT_LOGS_TO_APPHANCE_TASK_NAME)
        task.description = 'Converts all logs to apphance from android logs for the source project'
        task.group = AMEBA_APPHANCE_SERVICE
        task << { new ApphanceLogsConversionTask(project.ant).convertLogsToApphance(project.rootDir) }
    }

    private void prepareConvertLogsToAndroidTask() {
        def task = project.task(CONVERT_LOGS_TO_ANDROID_TASK_NAME)
        task.description = 'Converts all logs to android from apphance logs for the source project'
        task.group = AMEBA_APPHANCE_SERVICE
        task << { new AndroidLogsConversionTask(project.ant).convertLogsToAndroid(project.rootDir) }
    }

    private void prepareSingleBuildUploadTask(String variantName, String buildTaskName) {
        def task = project.task("upload${variantName.toLowerCase().capitalize()}")
        task.description = 'Uploads apk & image_montage to Apphance server'
        task.group = AMEBA_APPHANCE_SERVICE
        task << {
            new UploadAndroidArtifactTask(project, executor).uploadArtifact(variantName)
        }
        task.dependsOn(buildTaskName)
        task.dependsOn(PREPARE_IMAGE_MONTAGE_TASK_NAME)
    }

    static public final String DESCRIPTION =
        """This is the plugin that links Ameba with Apphance service.

The plugin provides integration with Apphance service. It performs the
following tasks: adding Apphance on-the-fly while building the application
(for all Debug builds), removing Apphance on-the-fly while building the application
(for all Release builds), submitting the application to apphance at release time.
"""
}
