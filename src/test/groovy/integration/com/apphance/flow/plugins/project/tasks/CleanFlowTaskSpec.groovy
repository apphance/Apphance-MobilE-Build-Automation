package com.apphance.flow.plugins.project.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.LOG_DIR
import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static org.gradle.testfixtures.ProjectBuilder.builder

class CleanFlowTaskSpec extends Specification {

    def 'appropriate directories are cleaned'() {
        given:
        def projectDir = new File('projects/test/android/android-basic')
        def project = builder().withProjectDir(projectDir).build()

        and:
        def pc = GroovySpy(ProjectConfiguration)
        pc.project = GroovyStub(Project) {
            file(TMP_DIR) >> project.file(TMP_DIR)
            file(LOG_DIR) >> project.file(LOG_DIR)
        }

        and:
        def task = project.task(CleanFlowTask.NAME, type: CleanFlowTask) as CleanFlowTask
        task.conf = pc

        when:
        task.clean()

        then:
        project.file(TMP_DIR).exists()
        project.file(TMP_DIR).list().size() == 0
        project.file(LOG_DIR).exists()
        project.file(LOG_DIR).list().size() == 0
    }
}
