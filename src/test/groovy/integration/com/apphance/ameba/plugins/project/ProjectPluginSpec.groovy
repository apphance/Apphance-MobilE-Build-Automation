package com.apphance.ameba.plugins.project

import com.apphance.ameba.plugins.project.tasks.CheckTestsTask
import com.apphance.ameba.plugins.project.tasks.CleanConfTask
import com.apphance.ameba.plugins.project.tasks.PrepareSetupTask
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(ProjectPlugin)

        then:
        project.repositories.mavenCentral()

        and:
        project.tasks[CleanConfTask.NAME].group == AMEBA_CONFIGURATION
        project.tasks[PrepareSetupTask.NAME].group == AMEBA_SETUP
        project.tasks[VerifySetupTask.NAME].group == AMEBA_SETUP
        project.tasks[CheckTestsTask.NAME].group == AMEBA_TEST
    }
}
