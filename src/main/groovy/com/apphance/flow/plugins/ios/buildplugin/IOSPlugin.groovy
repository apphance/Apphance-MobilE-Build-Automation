package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.buildplugin.tasks.*
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.CopySourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.api.logging.Logging.getLogger

/**
 * This is the main iOS plugin.
 * <br/><br/>
 * It provides all tasks necessary for building artifacts for the defined variants. Every variant has a related task,
 * that is dynamically created during initialization of Apphance Flow. For 'DEVICE' and 'SIMULATOR' variants build task
 * name is prefixed with 'archive' prefix, for 'FRAMEWORK' mode prefix for task name is 'framework'.
 * <br/><br/>
 * The following artifacts are be prepared with usage of iOS Plugin:
 * <br/>
 * <ul>
 *  <li>for 'DEVICE' mode variant:
 *      <ul>
 *          <li>zipped xcarchive</li>
 *          <li>zipped dSYM files</li>
 *          <li>zipped ahSYM files - special SYM format for apphance integration</li>
 *          <li>ipa file</li>
 *          <li>manifest file</li>
 *          <li>mobileprovision file</li>
 *      </ul>
 *  </li>
 *  <br/>
 *  <li>for 'SIMULATOR' mode variant
 *      <ul>
 *          <li>dmg disk image with iPhone simulator dedicated application</li>
 *          <li>dmg disk image with iPad simulator dedicated application</li>
 *      </ul>
 *  </li>
 *  <br/>
 *  <li>for 'FRAMEWORK' mode variant
 *      <ul>
 *          <li>ready-to-se framework file</li>
 *      </ul>
 *  </li>
 * </ul>
 * That's all greetings for the whole family!
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
