package com.apphance.ameba.plugins.projectconfiguration

import spock.lang.Specification

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.*
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
        project.tasks[CLEAN_CONFIGURATION_TASK_NAME].group == AMEBA_CONFIGURATION
        project.tasks[PREPARE_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[VERIFY_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[SHOW_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[CHECK_TESTS_TASK_NAME].group == AMEBA_TEST
        project.tasks[SHOW_CONVENTIONS_TASK_NAME].group == AMEBA_SETUP

        then: 'each task has correct dependency'
        project.tasks[CLEAN_CONFIGURATION_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[PREPARE_SETUP_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[VERIFY_SETUP_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[SHOW_SETUP_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[CHECK_TESTS_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)

        then: 'convention rule is added'
        project.tasks.rules*.description.contains('Pattern: showConvention<ConventionName>: Shows current convention values for convention specified by name')

    }
}
