package com.apphance.ameba.plugins.android.buildplugin

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.buildplugin.tasks.*
import com.apphance.ameba.plugins.project.tasks.PrepareSetupTask
import com.apphance.ameba.plugins.project.tasks.CleanConfTask
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME

/**
 * This is the main android build plugin.
 *
 * The plugin provides all the tasks needed to build android application.
 * Besides tasks explained below, the plugin prepares build-* and install-*
 * tasks which are dynamically created, based on variants available. In
 * case the build has no variants, the only available builds are Debug and Release.
 * In case of variants, there is one build and one task created for every variant.
 */
class AndroidPlugin implements Plugin<Project> {

    static final String BUILD_ALL_TASK_NAME = 'buildAll'
    static final String BUILD_ALL_DEBUG_TASK_NAME = 'buildAllDebug'
    static final String BUILD_ALL_RELEASE_TASK_NAME = 'buildAllRelease'

    @Inject AndroidConfiguration conf
    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {

        if (conf.isEnabled()) {

            project.task(UpdateProjectTask.NAME,
                    type: UpdateProjectTask)

            project.task(CleanClassesTask.NAME,
                    type: CleanClassesTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(CopySourcesTask.NAME,
                    type: CopySourcesTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(ReplacePackageTask.NAME,
                    type: ReplacePackageTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(CleanAndroidTask.NAME,
                    type: CleanAndroidTask,
                    dependsOn: [CleanConfTask.NAME, UpdateProjectTask.NAME])

            project.task(CLEAN_TASK_NAME) << {
                conf.buildDir.deleteDir()
            }

            project.tasks[CLEAN_TASK_NAME].dependsOn(CleanAndroidTask.NAME)

            project.task(CompileAndroidTask.NAME,
                    type: CompileAndroidTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(BUILD_ALL_DEBUG_TASK_NAME,
                    group: AMEBA_BUILD,
                    description: 'Builds all debug variants')

            project.task(BUILD_ALL_RELEASE_TASK_NAME,
                    group: AMEBA_BUILD,
                    description: 'Build all release variants')

            project.task(BUILD_ALL_TASK_NAME,
                    group: AMEBA_BUILD,
                    dependsOn: [BUILD_ALL_DEBUG_TASK_NAME, BUILD_ALL_RELEASE_TASK_NAME],
                    description: 'Builds all variants')

            variantsConf.variants.each { variant ->
                project.task(variant.buildTaskName,
                        type: SingleVariantTask,
                        dependsOn: [CopySourcesTask.NAME, UpdateProjectTask.NAME]).variant = variant

                def buildAllMode = "buildAll${variant.mode.capitalize()}"
                project.tasks[buildAllMode].dependsOn variant.buildTaskName

                project.task("install${variant.name}", type: InstallTask, dependsOn: variant.buildTaskName).variant = variant
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }
}
