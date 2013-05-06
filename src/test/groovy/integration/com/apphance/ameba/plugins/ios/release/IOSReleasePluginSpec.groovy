package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.ios.release.IOSReleasePlugin.UPDATE_VERSION_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSReleasePluginSpec extends Specification {


    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def irc = Mock(IOSReleaseConfiguration)
        irc.isEnabled() >> true

        and:
        def irp = new IOSReleasePlugin()
        irp.releaseConf = irc

        when:
        irp.apply(project)

        then:
        project.tasks[UPDATE_VERSION_TASK_NAME].group == AMEBA_RELEASE
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def irc = Mock(IOSReleaseConfiguration)
        irc.isEnabled() >> false

        and:
        def irp = new IOSReleasePlugin()
        irp.releaseConf = irc

        when:
        irp.apply(project)

        then:
        !project.getTasksByName(UPDATE_VERSION_TASK_NAME, false)
    }
}
