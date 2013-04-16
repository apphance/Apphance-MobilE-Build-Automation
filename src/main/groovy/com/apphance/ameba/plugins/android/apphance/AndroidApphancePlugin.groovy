package com.apphance.ameba.plugins.android.apphance

import com.apphance.ameba.configuration.android.AndroidApphanceConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.apphance.tasks.AndroidLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.ApphanceLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_IMAGE_MONTAGE_TASK_NAME

/**
 * Adds Apphance in automated way.
 *
 * This is the plugin that links Ameba with Apphance service.
 *
 * The plugin provides integration with Apphance service. It performs the
 * following tasks: adding Apphance on-the-fly while building the application
 * (for all Debug builds), removing Apphance on-the-fly while building the application//TODO
 * (for all Release builds), submitting the application to apphance at release time.
 *
 */
@Mixin(ApphancePluginCommons)
class AndroidApphancePlugin implements Plugin<Project> {

    private Project project
    @Inject
    private AndroidVariantsConfiguration variantsConf
    @Inject
    private AndroidApphanceConfiguration apphanceConf

    @Override
    void apply(Project project) {
        if (apphanceConf.isEnabled()) {
            this.project = project

            addApphanceConfiguration(project)
            preProcessBuildsWithApphance()

            project.task(ApphanceLogsConversionTask.NAME, type: ApphanceLogsConversionTask)
            project.task(AndroidLogsConversionTask.NAME, type: AndroidLogsConversionTask)
        }
    }

    private void preProcessBuildsWithApphance() {
        //TODO after AndroidPlugin
//        androidConf.buildableVariants.each { variant ->
//            if (androidConf.debugRelease.get(variant) == 'Debug') {
//                def task = project.tasks["buildDebug-$variant"]
//                task.doFirst {
//                    new AddAndroidApphanceTask(project).addApphance(variant)
//                }
//                prepareSingleBuildUploadTask(variant, task.name)
//            }
//        }
    }

    private void prepareSingleBuildUploadTask(String variantName, String buildTaskName) {
        def task = project.task("upload${variantName.toLowerCase().capitalize()}", type: UploadAndroidArtifactTask)
        task.variant = variantName
        task.dependsOn(buildTaskName)
        task.dependsOn(PREPARE_IMAGE_MONTAGE_TASK_NAME)
    }
}
