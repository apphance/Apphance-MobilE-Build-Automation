package com.apphance.ameba.plugins.ios.ocunit

import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import spock.lang.Specification

import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.AMEBA_IOS_UNIT
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSUnitTestPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        def conf = Stub(IOSUnitTestConfiguration)
        conf.isEnabled() >> true
        def plugin = new IOSUnitTestPlugin()
        plugin.iosUnitTestConf = conf
        plugin.apply(project)

        then: 'every single task is in correct group'
        project.tasks[RunUnitTestsTasks.NAME].group == AMEBA_IOS_UNIT

        and: 'task dependencies configured correctly'
        project.tasks[RunUnitTestsTasks.NAME].dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)

    }
}
