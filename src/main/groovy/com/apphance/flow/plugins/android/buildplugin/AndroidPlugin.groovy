package com.apphance.flow.plugins.android.buildplugin

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.flow.plugins.android.buildplugin.tasks.UpdateProjectTask
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.api.logging.Logging.getLogger

/**
 * Main android build plugin.<br/>
 *
 * The plugin provides all the tasks needed to build android application.
 * Among plugin tasks there are three static anchor tasks: 'buildAll', 'buildAllDebug' and 'buildAllRelease'.
 * They actually don't do anything besides calling other build tasks. Build tasks are created dynamically based on available variants.
 */
class AndroidPlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    static final String BUILD_ALL_TASK_NAME = 'buildAll'
    static final String BUILD_ALL_DEBUG_TASK_NAME = 'buildAllDebug'
    static final String BUILD_ALL_RELEASE_TASK_NAME = 'buildAllRelease'

    @Inject AndroidConfiguration conf
    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {

        if (conf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task(UpdateProjectTask.NAME,
                    type: UpdateProjectTask)

            project.task(CopySourcesTask.NAME,
                    type: CopySourcesTask).mustRunAfter(CleanFlowTask.NAME)

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
            }
        }
    }
}
