package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.plugins.projectconfiguration.tasks.CheckTestsTask
import com.apphance.ameba.plugins.projectconfiguration.tasks.CleanConfTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.*
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectConfigurationPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(ProjectConfigurationPlugin)

        then: 'convention is added'
        project.convention.plugins[AMEBA_PROPERTY_DEFAULTS_CONVENTION_NAME]

        then: 'maven central repo is added'
        project.repositories.mavenCentral()

        then: 'each task is in correct group'
        project.tasks[READ_PROJECT_CONFIGURATION_TASK_NAME].group == AMEBA_CONFIGURATION
        project.tasks[CleanConfTask.NAME].group == AMEBA_CONFIGURATION
        project.tasks[PREPARE_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[VERIFY_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[SHOW_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[CheckTestsTask.NAME].group == AMEBA_TEST
        project.tasks[SHOW_CONVENTIONS_TASK_NAME].group == AMEBA_SETUP

        then: 'each task has correct dependency'
        project.tasks[CleanConfTask.NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[PREPARE_SETUP_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[SHOW_SETUP_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[CheckTestsTask.NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)

        then: 'convention rule is added'
        project.tasks.rules*.description.contains('Pattern: showConvention<ConventionName>: Shows current convention values for convention specified by name')

    }
}
