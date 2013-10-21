package com.apphance.flow.plugins.android.release

import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.android.release.tasks.UpdateVersionTask
import com.apphance.flow.plugins.project.tasks.CopySourcesTask
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidReleasePluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {

        given:
        def project = builder().build()

        and:
        def arp = new AndroidReleasePlugin()

        and: 'create mock android release configuration and set it'
        def arc = Mock(AndroidReleaseConfiguration)
        arc.isEnabled() >> true
        arp.releaseConf = arc

        when:
        arp.apply(project)

        then: 'every single task is in correct group'
        project.tasks[UpdateVersionTask.NAME].group == FLOW_RELEASE.name()
        project.tasks[AvailableArtifactsInfoTask.NAME].group == FLOW_RELEASE.name()

        then:
        project.tasks[UpdateVersionTask.NAME].dependsOn.contains(CopySourcesTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def arp = new AndroidReleasePlugin()

        and: 'create mock android release configuration and set it'
        def arc = Mock(AndroidReleaseConfiguration)
        arc.isEnabled() >> false
        arp.releaseConf = arc

        when:
        arp.apply(project)

        then:
        !project.getTasksByName(UpdateVersionTask.NAME, false)
        !project.getTasksByName(AvailableArtifactsInfoTask.NAME, false)
    }
}
