package com.apphance.ameba.plugins.ios.ocunit

import spock.lang.Specification

import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.AMEBA_IOS_UNIT
import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.RUN_UNIT_TESTS_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSUnitTestPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(IOSUnitTestPlugin)

        then: 'convention is added'
        project.convention.plugins.iosUnitTests

        then: 'every single task is in correct group'
        project.tasks[RUN_UNIT_TESTS_TASK_NAME].group == AMEBA_IOS_UNIT

        and: 'task dependencies configured correctly'
        project.tasks[RUN_UNIT_TESTS_TASK_NAME].dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)

    }
}
