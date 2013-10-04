package com.apphance.flow.plugins.android.test

import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.android.test.tasks.RunRobolectricTestsTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin that provides test functionality for android project.<br/><br/>
 *
 * Currently Apphance Flow supports testing android app with Robolectric framework.
 * Plugin adds one anchor task 'testAll' and 'testVariantName' tasks for each configured variant.
 */
class AndroidTestPlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    static final String TEST_ALL_TASK_NAME = 'testAll'

    @Inject AndroidTestConfiguration testConf
    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {
        if (testConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${getClass().simpleName}")

            project.task(TEST_ALL_TASK_NAME, group: FLOW_TEST, description: 'Run all android tests in all variants')

            variantsConf.variants.each { AndroidVariantConfiguration variantConf ->
                def testTaskName = variantConf.testTaskName
                project.task(testTaskName,
                        type: RunRobolectricTestsTask,
                        dependsOn: CopySourcesTask.NAME).variantConf = variantConf

                project.tasks.findByName(TEST_ALL_TASK_NAME).dependsOn testTaskName
                project.tasks.findByName(testTaskName)?.dependsOn variantConf.buildTaskName
                project.tasks.findByName(testTaskName).mustRunAfter VerifySetupTask.NAME
            }
        }
    }
}