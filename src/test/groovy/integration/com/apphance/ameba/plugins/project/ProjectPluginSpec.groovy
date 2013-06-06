package com.apphance.ameba.plugins.project

import com.apphance.ameba.plugins.project.tasks.CleanFlowTask
import com.apphance.ameba.plugins.project.tasks.PrepareSetupTask
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_SETUP
import static com.apphance.ameba.plugins.project.ProjectPlugin.COPY_SOURCES_TASK_NAME
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class ProjectPluginSpec extends Specification {

    def projectDir = createTempDir()
    def project = builder().withProjectDir(projectDir).build()

    def cleanup() {
        projectDir.deleteDir()
    }

    def 'adds tasks when flow.properties exists'() {
        when:
        project.plugins.apply(ProjectPlugin)

        then:
        project.repositories.mavenCentral()

        and:
        project.tasks[PrepareSetupTask.NAME].group == FLOW_SETUP.name()
        !project.tasks.findByName(CleanFlowTask.NAME)
        !project.tasks.findByName(VerifySetupTask.NAME)
    }

    def 'adds tasks when flow.properties does not exist'() {
        given:
        new File(projectDir, 'flow.properties').createNewFile()

        when:
        project.plugins.apply(ProjectPlugin)

        then:
        project.tasks[PrepareSetupTask.NAME].group == FLOW_SETUP.name()
        project.tasks[CleanFlowTask.NAME].group == FLOW_SETUP.name()
        project.tasks[VerifySetupTask.NAME].group == FLOW_SETUP.name()

        and:
        project.tasks[VerifySetupTask.NAME].dependsOn.flatten().contains(COPY_SOURCES_TASK_NAME)
    }
}
