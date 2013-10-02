package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
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

/**
 * Plugin for various X-Code related tasks.
 *
 */
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
                        description: "Aggregate task, builds all 'DEVICE' mode variants.")

                project.task(ARCHIVE_ALL_SIMULATOR_TASK_NAME,
                        group: FLOW_BUILD,
                        description: "Aggregate task, builds all 'SIMULATOR' mode variants.")

                project.task(ARCHIVE_ALL_TASK_NAME,
                        group: FLOW_BUILD,
                        dependsOn: [ARCHIVE_ALL_DEVICE_TASK_NAME, ARCHIVE_ALL_SIMULATOR_TASK_NAME],
                        description: "Aggregate task, builds all 'DEVICE' and 'SIMULATOR' variants.")

                buildableVariants.findAll { it.mode.value == DEVICE }.each(this.&createArchiveDeviceTask)
                buildableVariants.findAll { it.mode.value == SIMULATOR }.each(this.&createArchiveSimulatorTask)
            }

            def frameworkVariants = variantsConf.variants.findAll { it.mode.value == FRAMEWORK }

            if (frameworkVariants) {
                project.task(FRAMEWORK_ALL,
                        group: FLOW_BUILD,
                        description: "Aggregate task, builds all 'FRAMEWORK' mode variants.")

                frameworkVariants.each(this.&createFrameworkVariant)
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME, CopySourcesTask.NAME, CleanFlowTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }

    private void createArchiveDeviceTask(AbstractIOSVariant variant) {
        def task = project.task(variant.archiveTaskName,
                type: DeviceVariantTask,
                dependsOn: [CopyMobileProvisionTask.NAME],
                description: "Invokes 'archive' action for the variant. From the result of the action xcarchive, " +
                        "distribution zip, dSYM, ahSYM, ipa, manifest and mobileprovision artifacts are prepared. " +
                        "All the artifacts are located under $ReleaseConfiguration.OTA_DIR."
        ) as DeviceVariantTask

        task.variant = variant
        project.tasks[ARCHIVE_ALL_DEVICE_TASK_NAME].dependsOn task.name
    }

    private void createArchiveSimulatorTask(AbstractIOSVariant variant) {
        def task = project.task(variant.archiveTaskName,
                type: SimulatorVariantTask,
                dependsOn: [CopySourcesTask.NAME],
                description: "Invokes 'build' action for the variant. From the result of the action simulator " +
                        "images are prepared for both iPhone and iPad simulators. " +
                        "Images are located under $ReleaseConfiguration.OTA_DIR."
        ) as SimulatorVariantTask
        task.variant = variant
        project.tasks[ARCHIVE_ALL_SIMULATOR_TASK_NAME].dependsOn task.name
    }

    private void createFrameworkVariant(AbstractIOSVariant variant) {
        def task = project.task(variant.frameworkTaskName,
                type: FrameworkVariantTask,
                dependsOn: [CopySourcesTask.NAME],
                description:
                        "Prepares a ready-to-use framework and puts the artifact under $ReleaseConfiguration.OTA_DIR."
        ) as FrameworkVariantTask
        task.variant = variant
        project.tasks[FRAMEWORK_ALL].dependsOn task.name
    }
}
