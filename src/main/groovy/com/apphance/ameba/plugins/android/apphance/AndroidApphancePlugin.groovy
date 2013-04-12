package com.apphance.ameba.plugins.android.apphance

import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.apphance.tasks.AddAndroidApphanceTask
import com.apphance.ameba.plugins.android.apphance.tasks.AndroidLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.ApphanceLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import com.apphance.ameba.plugins.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.plugins.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.plugins.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_IMAGE_MONTAGE_TASK_NAME

/**
 * Adds Apphance in automated way.
 *
 */
@Mixin(ApphancePluginCommons)
class AndroidApphancePlugin implements Plugin<Project> {

    private Project project
    private AndroidProjectConfiguration androidConf

    @Override
    void apply(Project project) {
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)

        addApphanceConfiguration(project)
        preProcessBuildsWithApphance()

        project.task(ApphanceLogsConversionTask.taskName, type: ApphanceLogsConversionTask)
        project.task(AndroidLogsConversionTask.taskName, type: AndroidLogsConversionTask)
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

    private void prepareSingleBuildUploadTask(String variantName, String buildTaskName) {
        def task = project.task("upload${variantName.toLowerCase().capitalize()}", type: UploadAndroidArtifactTask)
        task.variant = variantName
        task.dependsOn(buildTaskName)
        task.dependsOn(PREPARE_IMAGE_MONTAGE_TASK_NAME)
    }

    static public final String DESCRIPTION =
        """ |This is the plugin that links Ameba with Apphance service.
            |
            |The plugin provides integration with Apphance service. It performs the
            |following tasks: adding Apphance on-the-fly while building the application
            |(for all Debug builds), removing Apphance on-the-fly while building the application
            |(for all Release builds), submitting the application to apphance at release time.
            |""".stripMargin()
}
