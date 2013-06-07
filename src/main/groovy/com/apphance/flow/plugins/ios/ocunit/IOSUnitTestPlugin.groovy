package com.apphance.flow.plugins.ios.ocunit

import com.apphance.flow.configuration.ios.IOSUnitTestConfiguration
import com.apphance.flow.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Unit test plugin - all unit tests are run here.
 *
 * This plugins provides functionality of standard ocunit testing for iOS.
 * It executes all tests which are build using ocunit test framework.
 * More description needed ....
 *
 */
class IOSUnitTestPlugin implements Plugin<Project> {

    @Inject IOSUnitTestConfiguration unitTestConf

    @Override
    void apply(Project project) {
        if (unitTestConf.isEnabled()) {
            project.task(RunUnitTestsTasks.NAME, type: RunUnitTestsTasks)
        }
    }
}
