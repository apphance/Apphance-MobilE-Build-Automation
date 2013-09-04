package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.buildplugin.tasks.*
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.PrepareSetupTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.api.logging.Logging.getLogger

class IOSPlugin implements Plugin<Project> {

    public static final String ARCHIVE_ALL_TASK_NAME = 'archiveAll'
    public static final String ARCHIVE_ALL_DEVICE_TASK_NAME = 'archiveAllDevice'
    public static final String ARCHIVE_ALL_SIMULATOR_TASK_NAME = 'archiveAllSimulator'
    public static final String FRAMEWORK_ALL = 'frameworkAll'

    private logger = getLogger(getClass())

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSExecutor executor

    private Project project

    @Override
    void apply(Project project) {
        if (conf.isEnabled()) {
            this.project = project
            logger.lifecycle("Applying plugin ${getClass().simpleName}")

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

            def buildableVariants = variantsConf.variants.findAll { it.mode.value in [DEVICE, SIMULATOR] }

            if (buildableVariants) {
                project.task(ARCHIVE_ALL_DEVICE_TASK_NAME,
                        group: FLOW_BUILD,
                        description: 'Archives all device variants')

                project.task(ARCHIVE_ALL_SIMULATOR_TASK_NAME,
                        group: FLOW_BUILD,
                        description: 'Archives all simulator variants')

                project.task(ARCHIVE_ALL_TASK_NAME,
                        group: FLOW_BUILD,
                        dependsOn: [ARCHIVE_ALL_DEVICE_TASK_NAME, ARCHIVE_ALL_SIMULATOR_TASK_NAME],
                        description: 'Archives all variants and produces all artifacts (zip, ipa, messages, etc)')
                buildableVariants.each(this.&createArchiveTask)
            }

            def frameworkVariants = variantsConf.variants.findAll { it.mode.value == FRAMEWORK }

            if (frameworkVariants) {
                project.task(FRAMEWORK_ALL,
                        group: FLOW_BUILD,
                        description: 'Builds all framework variants')

                frameworkVariants.each(this.&createFrameworkVariant)
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME, CopySourcesTask.NAME, CleanFlowTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }

    private void createArchiveTask(IOSVariant variant) {
        def task = project.task(variant.archiveTaskName,
                type: ArchiveVariantTask,
                dependsOn: [CopyMobileProvisionTask.NAME]) as ArchiveVariantTask
        task.variant = variant
        def archiveAllMode = "archiveAll${variant.mode.value.capitalize()}"
        project.tasks[archiveAllMode].dependsOn task.name
    }

    private void createFrameworkVariant(IOSVariant variant) {
        def task = project.task(variant.frameworkTaskName,
                type: FrameworkVariantTask,
                dependsOn: [CopySourcesTask.NAME]) as FrameworkVariantTask
        task.variant = variant
        project.tasks[FRAMEWORK_ALL].dependsOn task.name
    }
}
