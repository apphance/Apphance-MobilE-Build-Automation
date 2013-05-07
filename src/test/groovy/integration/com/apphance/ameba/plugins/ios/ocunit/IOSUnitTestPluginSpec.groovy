package com.apphance.ameba.plugins.ios.ocunit

import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import spock.lang.Specification

import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.AMEBA_IOS_UNIT
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSUnitTestPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def conf = Stub(IOSUnitTestConfiguration)
        conf.isEnabled() >> true

        and:
        def plugin = new IOSUnitTestPlugin()
        plugin.iosUnitTestConf = conf

        when:
        plugin.apply(project)

        then:
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
