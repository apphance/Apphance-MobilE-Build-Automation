package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.plugins.projectconfiguration.tasks.CheckTestsTask
import com.apphance.ameba.plugins.projectconfiguration.tasks.CleanConfTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.*
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.PREPARE_SETUP_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.VERIFY_SETUP_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectConfigurationPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(ProjectConfigurationPlugin)

        then: 'maven central repo is added'
        project.repositories.mavenCentral()

        then: 'each task is in correct group'
        project.tasks[CleanConfTask.NAME].group == AMEBA_CONFIGURATION
        project.tasks[PREPARE_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[VERIFY_SETUP_TASK_NAME].group == AMEBA_SETUP
        project.tasks[CheckTestsTask.NAME].group == AMEBA_TEST
    }
}
