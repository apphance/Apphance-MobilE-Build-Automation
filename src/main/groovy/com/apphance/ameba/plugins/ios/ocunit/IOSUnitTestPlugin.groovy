package com.apphance.ameba.plugins.ios.ocunit

import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Unit test plugin - all unit tests are run here.
 *
 * This plugins provides functionality of standard ocunit testing for iOS.
 * It executes all tests which are build using ocunit test framework.
 * More description needed ....
 *
 */
class IOSUnitTestPlugin implements Plugin<Project> {

    public static final String AMEBA_IOS_UNIT = 'Ameba iOS OCUnit tests'

    @Inject
    IOSUnitTestConfiguration iosUnitTestConf

    @Override
    void apply(Project project) {
        if (iosUnitTestConf.isEnabled()) {
            project.task(RunUnitTestsTasks.NAME, type: RunUnitTestsTasks)
        }
    }
}
