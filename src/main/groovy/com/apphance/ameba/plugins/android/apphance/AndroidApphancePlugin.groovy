package com.apphance.ameba.plugins.android.apphance

import com.apphance.ameba.configuration.android.AndroidApphanceConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.apphance.tasks.AddApphanceToAndroid
import com.apphance.ameba.plugins.android.apphance.tasks.AndroidLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.ApphanceLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import com.apphance.ameba.plugins.release.tasks.ImageMontageTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.BUILD_ALL_DEBUG_TASK_NAME
import static org.gradle.api.logging.Logging.getLogger

/**
 * Adds Apphance in automated way.
 *
 * This is the plugin that links Ameba with Apphance service.
 *
 * The plugin provides integration with Apphance service. It performs the
 * following tasks: adding Apphance on-the-fly while building the application
 * (for all Debug builds), removing Apphance on-the-fly while building the application
 * (for all Release builds), submitting the application to apphance at release time.
 *
 */
//TODO this is class is still to be refactored after configuration for android is finished
//TODO Apphance Setup Task
@Mixin(ApphancePluginCommons)
class AndroidApphancePlugin implements Plugin<Project> {

    def log = getLogger(this.class)

    private Project project
    @Inject
    private AndroidVariantsConfiguration variantsConf
    @Inject
    private AndroidApphanceConfiguration apphanceConf
    @Inject
    private AddApphanceToAndroid addAndroidApphance

    @Override
    void apply(Project project) {
        if (apphanceConf.isEnabled()) {
            this.project = project

            addApphanceConfiguration(project)
            preProcessBuildsWithApphance()

            //TODO probably both to be removed
            def t1 = project.task(ApphanceLogsConversionTask.NAME,
                    group: AMEBA_APPHANCE_SERVICE,
                    description: 'Converts all logs to apphance from android logs for the source project')
            t1 << {
                new ApphanceLogsConversionTask(project.ant).convertLogsToApphance(project.rootDir)
            }
            def t2 = project.task(AndroidLogsConversionTask.NAME,
                    group: AMEBA_APPHANCE_SERVICE,
                    description: 'Converts all logs to android from apphance logs for the source project')
            t2 << {
                new AndroidLogsConversionTask(project.ant).convertLogsToAndroid(project.rootDir)
            }
        }
    }

    private void preProcessBuildsWithApphance() {
        //TODO for each variant add apphance if it's enabled in variant conf
        variantsConf.variants.each { avc ->
            if (avc.mode == DEBUG) {
                log.lifecycle("Adding apphance task for ${avc.name}")
                def task = project.task(avc.name, dependsOn: "build${avc.name}")
                project.tasks[BUILD_ALL_DEBUG_TASK_NAME].dependsOn task
                task.doFirst {
                    addAndroidApphance.addApphance(avc)
                }
                prepareSingleBuildUploadTask(avc, task.name)
            } else {
                log.lifecycle("Not adding apphance to ${avc.name} because it is not in debug mode")
            }
        }
    }

    private void prepareSingleBuildUploadTask(AndroidVariantConfiguration variant, String buildTaskName) {
        def task = project.task("upload${variant.name.toLowerCase().capitalize()}", type: UploadAndroidArtifactTask) as UploadAndroidArtifactTask
        task.variant = variant
        task.dependsOn(buildTaskName)
        task.dependsOn(ImageMontageTask.NAME)
    }
}
