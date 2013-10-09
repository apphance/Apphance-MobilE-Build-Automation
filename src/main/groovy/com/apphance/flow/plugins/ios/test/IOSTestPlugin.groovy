package com.apphance.flow.plugins.ios.test

import com.apphance.flow.configuration.ios.IOSTestConfiguration
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.ios.test.tasks.IOSTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static org.gradle.api.logging.Logging.getLogger

/**
 * This plugin enables running ocunit compatible tests for chosen variants.
 * <br/><br/>
 * To enable this plugin following requirements must be fulfilled:
 * <ul>
 *     <li>xcode version must be between 4.6.2 and 5.0</li>
 *     <li>ios-sim must be installed</li>
 *     <li>test targets must be enabled</li>
 * </ul>
 * Running tests is done by adding special shell script to build phase in project.pbxproj configuration file and then
 * invoking 'build' action for particular target and configuration.
 */
class IOSTestPlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    public static final String TEST_ALL_TASK_NAME = 'testAll'

    @Inject IOSTestConfiguration testConf

    @Override
    void apply(Project project) {
        if (testConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${getClass().simpleName}")

            if (testConf.testVariants.size() > 0) {

                project.task(TEST_ALL_TASK_NAME,
                        group: FLOW_TEST,
                        description: "Aggregate task, runs tests for all variants configured in 'ios.test.variants'.")

                testConf.testVariants.each { variant ->

                    def testTask = project.task(variant.testTaskName,
                            type: IOSTestTask,
                            dependsOn: CopySourcesTask.NAME,
                    ) as IOSTestTask

                    testTask.variant = variant

                    project.tasks[TEST_ALL_TASK_NAME].dependsOn variant.testTaskName

                    project.tasks.findByName(variant.archiveTaskName)?.dependsOn variant.testTaskName
                }
            }
        }
    }
}
