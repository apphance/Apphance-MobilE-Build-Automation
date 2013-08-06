package com.apphance.flow.plugins.ios.ocunit

import com.apphance.flow.configuration.ios.IOSTestConfiguration
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.ios.ocunit.tasks.IOSTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static org.gradle.api.logging.Logging.getLogger

/**
 * Unit test plugin - all unit tests are run here.
 *
 * This plugins provides functionality of standard ocunit testing for iOS.
 * It executes all tests which are build using ocunit test framework.
 * More description needed ....
 *
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

                project.task(TEST_ALL_TASK_NAME, group: FLOW_TEST)

                testConf.testVariants.each { variant ->
                    def testTask = project.task(variant.testTaskName,
                            type: IOSTestTask,
                            dependsOn: CopySourcesTask.NAME,
                    ) as IOSTestTask
                    testTask.variant = variant
                    project.tasks[TEST_ALL_TASK_NAME].dependsOn variant.testTaskName
                    //TODO archive and build dependency
                }
            }
        }
    }
}
