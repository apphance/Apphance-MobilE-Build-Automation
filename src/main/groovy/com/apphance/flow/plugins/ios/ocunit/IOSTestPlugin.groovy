package com.apphance.flow.plugins.ios.ocunit

import com.apphance.flow.configuration.ios.IOSTestConfiguration
import com.apphance.flow.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

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

    @Inject IOSTestConfiguration unitTestConf

    @Override
    void apply(Project project) {
        if (unitTestConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")
            project.task(RunUnitTestsTasks.NAME, type: RunUnitTestsTasks)
        }
    }
}
