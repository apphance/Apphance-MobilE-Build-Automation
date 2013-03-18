package com.apphance.ameba.plugins.release

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import spock.lang.Specification

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.*
import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectReleasePluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        and: 'setup operations are defined in ProjectConfigurationPlugin'
        project.plugins.apply(ProjectConfigurationPlugin)

        when:
        project.plugins.apply(ProjectReleasePlugin)

        then: 'project mail configuration was added'
        project.configurations.mail

        then: 'mail configuration dependencies are present'
        project.dependencies.configurationContainer.mail.allDependencies.size() == 3

        then: 'every task exists and is in correct group'
        project.tasks[COPY_GALLERY_FILES_TASK_NAME].group == AMEBA_CONFIGURATION
        project.tasks[PREPARE_FOR_RELEASE_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[VERIFY_RELEASE_NOTES_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[PREPARE_IMAGE_MONTAGE_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[SEND_MAIL_MESSAGE_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[CLEAN_RELEASE_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[BUILD_SOURCES_ZIP_TASK_NAME].group == AMEBA_RELEASE

        then: 'each task has correct dependency'
        project.tasks[COPY_GALLERY_FILES_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[PREPARE_FOR_RELEASE_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                COPY_GALLERY_FILES_TASK_NAME)
        project.tasks[VERIFY_RELEASE_NOTES_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)
        project.tasks[PREPARE_IMAGE_MONTAGE_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)
        project.tasks[SEND_MAIL_MESSAGE_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME,
                VERIFY_RELEASE_NOTES_TASK_NAME)
        project.tasks[CLEAN_RELEASE_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                CLEAN_TASK_NAME)
        project.tasks[BUILD_SOURCES_ZIP_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)
    }
}
