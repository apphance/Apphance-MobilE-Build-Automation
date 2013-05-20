package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.plugins.ios.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.ios.release.tasks.PrepareMailMessageTask
import com.apphance.ameba.plugins.release.tasks.AbstractUpdateVersionTask
import com.apphance.ameba.plugins.release.tasks.PrepareForReleaseTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
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
        project.tasks[AbstractUpdateVersionTask.NAME].group == AMEBA_RELEASE
        project.tasks[PrepareMailMessageTask.NAME].group == AMEBA_RELEASE
        project.tasks[AvailableArtifactsInfoTask.NAME].group == AMEBA_RELEASE

        and:
        project.tasks[PrepareMailMessageTask.NAME].dependsOn.flatten().containsAll(AvailableArtifactsInfoTask.NAME, PrepareForReleaseTask.NAME)
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
        !project.getTasksByName(AbstractUpdateVersionTask.NAME, false)
        !project.getTasksByName(PrepareMailMessageTask.NAME, false)
        !project.getTasksByName(AvailableArtifactsInfoTask.NAME, false)
    }
}
