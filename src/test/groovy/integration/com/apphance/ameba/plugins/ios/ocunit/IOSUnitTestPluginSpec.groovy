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
        def plugin = new IOSUnitTestPlugin()
        plugin.iosUnitTestConf = Stub(IOSUnitTestConfiguration, { isEnabled() >> true })

        when:
        plugin.apply(project)

        then: 'every single task is in correct group'
        project.tasks[RunUnitTestsTasks.NAME].group == AMEBA_IOS_UNIT
    }
    
    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def conf = Stub(IOSUnitTestConfiguration)
        conf.isEnabled() >> false

        and:
        def plugin = new IOSUnitTestPlugin()
        plugin.iosUnitTestConf = conf

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(RunUnitTestsTasks.NAME, false)
    }
}
