package com.apphance.ameba.plugins.release

import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.project.tasks.CleanFlowTask
import com.apphance.ameba.plugins.release.tasks.BuildSourcesZipTask
import com.apphance.ameba.plugins.release.tasks.ImageMontageTask
import com.apphance.ameba.plugins.release.tasks.SendMailMessageTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectReleasePluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)

        and:
        def prp = new ProjectReleasePlugin()

        and: 'create mock release configuration and set it'
        def rc = Mock(ReleaseConfiguration)
        rc.isEnabled() >> true
        prp.releaseConf = rc

        when:
        prp.apply(project)

        then: 'project mail configuration was added'
        project.configurations.mail

        then: 'mail configuration dependencies are present'
        project.dependencies.configurationContainer.mail.allDependencies.size() == 3

        then: 'every task exists and is in correct group'
        project.tasks[ImageMontageTask.NAME].group == AMEBA_RELEASE
        project.tasks[SendMailMessageTask.NAME].group == AMEBA_RELEASE
        project.tasks[BuildSourcesZipTask.NAME].group == AMEBA_RELEASE

        then: 'each task has correct dependency'
        project.tasks[SendMailMessageTask.NAME].dependsOn.flatten().contains('prepareMailMessage')
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def prp = new ProjectReleasePlugin()

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
