package com.apphance.flow.plugins.android.buildplugin

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.plugins.android.buildplugin.tasks.*
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.PrepareSetupTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.api.logging.Logging.getLogger

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

    private logger = getLogger(getClass())

    static final String BUILD_ALL_TASK_NAME = 'buildAll'
    static final String BUILD_ALL_DEBUG_TASK_NAME = 'buildAllDebug'
    static final String BUILD_ALL_RELEASE_TASK_NAME = 'buildAllRelease'

    @Inject AndroidConfiguration conf
    @Inject AndroidVariantsConfiguration variantsConf
    @Inject AntExecutor antExecutor

    @Override
    void apply(Project project) {

        if (conf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task(UpdateProjectTask.NAME,
                    type: UpdateProjectTask)

            project.task(CopySourcesTask.NAME,
                    type: CopySourcesTask).mustRunAfter(CleanFlowTask.NAME)

            project.task(ReplacePackageTask.NAME,
                    type: ReplacePackageTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.tasks.findByName(CleanFlowTask.NAME) << {
                def buildXml = new File(conf.rootDir, 'build.xml')
                if (buildXml.exists())
                    antExecutor.executeTarget(conf.rootDir, CLEAN)
                else
                    logger.lifecycle("Skipping 'ant clean' in dir: $conf.rootDir. File $buildXml.absolutePath does not exist")
            }

            project.task(CompileAndroidTask.NAME,
                    type: CompileAndroidTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(BUILD_ALL_DEBUG_TASK_NAME,
                    group: FLOW_BUILD,
                    description: 'Builds all debug variants')

            project.task(BUILD_ALL_RELEASE_TASK_NAME,
                    group: FLOW_BUILD,
                    description: 'Build all release variants')

            project.task(BUILD_ALL_TASK_NAME,
                    group: FLOW_BUILD,
                    dependsOn: [BUILD_ALL_DEBUG_TASK_NAME, BUILD_ALL_RELEASE_TASK_NAME],
                    description: 'Builds all variants')

            variantsConf.variants.each { variant ->
                project.task(variant.buildTaskName,
                        type: SingleVariantTask,
                        dependsOn: CopySourcesTask.NAME).variant = variant

                def buildAllMode = "buildAll${variant.mode.capitalize()}"
                project.tasks[buildAllMode].dependsOn variant.buildTaskName

                project.task("install${variant.name}", type: InstallAndroidTask, dependsOn: variant.buildTaskName).variant = variant
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME, CopySourcesTask.NAME, CleanFlowTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }
}
