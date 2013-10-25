package com.apphance.flow.plugins.project

import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.CopySourcesTask
import com.apphance.flow.plugins.project.tasks.PrepareSetupTask
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_SETUP
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
        project.tasks[PrepareSetupTask.NAME].group == FLOW_SETUP.toString()
        !project.tasks.findByName(CleanFlowTask.NAME)
        !project.tasks.findByName(CopySourcesTask.NAME)
    }

    def 'adds tasks when flow.properties does not exist'() {
        given:
        new File(projectDir, 'flow.properties').createNewFile()

        when:
        project.plugins.apply(ProjectPlugin)

        then:
        project.tasks[PrepareSetupTask.NAME].group == FLOW_SETUP.toString()
        def clean = project.tasks[CleanFlowTask.NAME]
        clean.group == FLOW_SETUP.toString()
        def copy = project.tasks[CopySourcesTask.NAME]
        copy.group == FLOW_BUILD.toString()
        copy.mustRunAfter.getDependencies(copy).contains(clean)
    }
}
