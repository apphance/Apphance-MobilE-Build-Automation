package com.apphance.ameba.plugins.android.buildplugin

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.buildplugin.tasks.*
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
    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {

        if (conf.isEnabled()) {

            project.task(UpdateProjectTask.NAME, type: UpdateProjectTask)

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

            project.task(BUILD_ALL_DEBUG_TASK_NAME, group: AMEBA_BUILD)
            project.task(BUILD_ALL_RELEASE_TASK_NAME, group: AMEBA_BUILD)
            project.task('buildAll', dependsOn: [BUILD_ALL_DEBUG_TASK_NAME, BUILD_ALL_RELEASE_TASK_NAME], group: AMEBA_BUILD)

            variantsConf.variants.each {
                def buildName = "build${it.name}"
                project.task(buildName,
                        type: SingleVariantTask,
                        dependsOn: [CopySourcesTask.NAME, UpdateProjectTask.NAME]).variant = it

                def buildAllMode = "buildAll${it.mode.capitalize()}"
                project.tasks[buildAllMode].dependsOn buildName

                project.task("install${it.name}", type: InstallTask, dependsOn: buildName).variant = it
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, 'prepareSetup'])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }
}
