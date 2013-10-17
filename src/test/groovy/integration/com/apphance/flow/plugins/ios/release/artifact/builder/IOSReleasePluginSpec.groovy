package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.ios.release.IOSReleasePlugin
import com.apphance.flow.plugins.ios.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
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
        project.tasks[AbstractUpdateVersionTask.NAME].group == FLOW_RELEASE.name()
        project.tasks[AvailableArtifactsInfoTask.NAME].group == FLOW_RELEASE.name()

        then:
        project.tasks[AbstractUpdateVersionTask.NAME].dependsOn.contains(CopySourcesTask.NAME)
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
        !project.getTasksByName(AvailableArtifactsInfoTask.NAME, false)
    }
}
