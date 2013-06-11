package com.apphance.flow.plugins.ios.ocunit

import com.apphance.flow.configuration.ios.IOSUnitTestConfiguration
import com.apphance.flow.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSUnitTestPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()
        def plugin = new IOSUnitTestPlugin()
        plugin.unitTestConf = Stub(IOSUnitTestConfiguration, { isEnabled() >> true })

        when:
        plugin.apply(project)

        then: 'every single task is in correct group'
        project.tasks[RunUnitTestsTasks.NAME].group == FLOW_TEST.name()
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def conf = Stub(IOSUnitTestConfiguration)
        conf.isEnabled() >> false

        and:
        def plugin = new IOSUnitTestPlugin()
        plugin.unitTestConf = conf

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(RunUnitTestsTasks.NAME, false)
    }
}
