package com.apphance.ameba.plugins.release

import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.plugins.release.tasks.*
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectReleasePluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

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
        project.tasks[CopyGalleryFilesTask.NAME].group == AMEBA_CONFIGURATION
        project.tasks[PrepareForReleaseTask.NAME].group == AMEBA_RELEASE
        project.tasks[VerifyReleaseNotesTask.NAME].group == AMEBA_RELEASE
        project.tasks[ImageMontageTask.NAME].group == AMEBA_RELEASE
        project.tasks[SendMailMessageTask.NAME].group == AMEBA_RELEASE
        project.tasks[CleanReleaseTask.NAME].group == AMEBA_RELEASE
        project.tasks[BuildSourcesZipTask.NAME].group == AMEBA_RELEASE

        then: 'each task has correct dependency'
        project.tasks[PrepareForReleaseTask.NAME].dependsOn.flatten().containsAll(CopyGalleryFilesTask.NAME)
        project.tasks[VerifyReleaseNotesTask.NAME].dependsOn.flatten().containsAll(PrepareForReleaseTask.NAME)
        project.tasks[ImageMontageTask.NAME].dependsOn.flatten().containsAll(PrepareForReleaseTask.NAME)
        project.tasks[SendMailMessageTask.NAME].dependsOn.flatten().containsAll(
                PrepareForReleaseTask.NAME,
                VerifyReleaseNotesTask.NAME,
                'prepareMailMessage')
        project.tasks[CleanReleaseTask.NAME].dependsOn.flatten().containsAll(CLEAN_TASK_NAME)
        project.tasks[BuildSourcesZipTask.NAME].dependsOn.flatten().containsAll(PrepareForReleaseTask.NAME)
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
        !project.getTasksByName(CleanReleaseTask.NAME, false)
        !project.getTasksByName(CopyGalleryFilesTask.NAME, false)
        !project.getTasksByName(ImageMontageTask.NAME, false)
        !project.getTasksByName(PrepareForReleaseTask.NAME, false)
        !project.getTasksByName(SendMailMessageTask.NAME, false)
        !project.getTasksByName(VerifyReleaseNotesTask.NAME, false)
    }
}
