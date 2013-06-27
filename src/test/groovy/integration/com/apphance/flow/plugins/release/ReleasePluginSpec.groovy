package com.apphance.flow.plugins.release

import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.release.tasks.BuildSourcesZipTask
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import com.apphance.flow.plugins.release.tasks.SendMailMessageTask
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static org.gradle.testfixtures.ProjectBuilder.builder

class ReleasePluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)

        and:
        project.task('prepareAvailableArtifactsInfo')

        and:
        def prp = new ReleasePlugin()

        and: 'create mock release configuration and set it'
        def rc = Mock(ReleaseConfiguration)
        rc.isEnabled() >> true
        prp.releaseConf = rc

        expect:
        project.tasks[CleanFlowTask.NAME].actions.size() == 0

        when:
        prp.apply(project)

        then: 'project mail configuration was added'
        project.configurations.mail

        then: 'mail configuration dependencies are present'
        project.dependencies.configurationContainer.mail.allDependencies.size() == 3

        then: 'every task exists and is in correct group'
        project.tasks[ImageMontageTask.NAME].group == FLOW_RELEASE.name()
        project.tasks[SendMailMessageTask.NAME].group == FLOW_RELEASE.name()
        project.tasks[BuildSourcesZipTask.NAME].group == FLOW_RELEASE.name()

        then:
        project.tasks[SendMailMessageTask.NAME].dependsOn.flatten().contains('prepareAvailableArtifactsInfo')

        then:
        project.tasks[CleanFlowTask.NAME].actions.size() > 0
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def prp = new ReleasePlugin()

        and: 'create mock release configuration and set it'
        def rc = Mock(ReleaseConfiguration)
        rc.isEnabled() >> false
        prp.releaseConf = rc

        when:
        prp.apply(project)

        then:
        !project.configurations.findByName('mail')

        then:
        !project.getTasksByName(BuildSourcesZipTask.NAME, false)
        !project.getTasksByName(ImageMontageTask.NAME, false)
        !project.getTasksByName(SendMailMessageTask.NAME, false)
    }
}
