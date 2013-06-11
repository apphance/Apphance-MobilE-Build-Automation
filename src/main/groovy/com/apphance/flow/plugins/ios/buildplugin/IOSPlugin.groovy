package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.ios.buildplugin.tasks.SingleVariantTask
import com.apphance.flow.plugins.ios.buildplugin.tasks.UnlockKeyChainTask
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.PrepareSetupTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

/*
 * Plugin for various X-Code related tasks.
 * This is the main iOS build plugin.
 *
 * The plugin provides all the task needed to build iOS application.
 * Besides tasks explained below, the plugin prepares build-*
 * tasks which are dynamically created, based on targets and configurations available.
 * There is one task available per each Target-Configuration combination - unless particular
 * combination is excluded by the exclude property.
 *
 */
class IOSPlugin implements Plugin<Project> {

    static final String BUILD_ALL_TASK_NAME = 'buildAll'
    static final String BUILD_ALL_DEVICE_TASK_NAME = 'buildAllDevice'
    static final String BUILD_ALL_SIMULATOR_TASK_NAME = 'buildAllSimulator'

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSExecutor executor

    @Override
    void apply(Project project) {
        if (conf.isEnabled()) {

            project.tasks.findByName(CleanFlowTask.NAME) << {
                executor.clean()
            }

            project.task(CopySourcesTask.NAME,
                    type: CopySourcesTask).mustRunAfter(CleanFlowTask.NAME)

            project.task(CopyMobileProvisionTask.NAME,
                    type: CopyMobileProvisionTask,
                    dependsOn: CopySourcesTask.NAME
            )

            project.task(UnlockKeyChainTask.NAME,
                    type: UnlockKeyChainTask)

            project.task(BUILD_ALL_DEVICE_TASK_NAME,
                    group: FLOW_BUILD,
                    description: 'Builds all device variants')

            project.task(BUILD_ALL_SIMULATOR_TASK_NAME,
                    group: FLOW_BUILD,
                    description: 'Builds all simulator variants')

            project.task(BUILD_ALL_TASK_NAME,
                    group: FLOW_BUILD,
                    dependsOn: [BUILD_ALL_DEVICE_TASK_NAME, BUILD_ALL_SIMULATOR_TASK_NAME],
                    description: 'Builds all variants and produces all artifacts (zip, ipa, messages, etc)')

            variantsConf.variants.each { variant ->
                def buildTask = project.task(variant.buildTaskName,
                        type: SingleVariantTask,
                        dependsOn: [CopyMobileProvisionTask.NAME]
                ) as SingleVariantTask
                buildTask.variant = variant

                def buildAllMode = "buildAll${variant.mode.value.capitalize()}"
                project.tasks[buildAllMode].dependsOn variant.buildTaskName
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME, CopySourcesTask.NAME, CleanFlowTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }
}
