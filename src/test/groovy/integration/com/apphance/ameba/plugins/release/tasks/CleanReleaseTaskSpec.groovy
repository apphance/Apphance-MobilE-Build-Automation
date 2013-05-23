package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import spock.lang.Specification

import static com.apphance.ameba.configuration.AbstractConfiguration.TMP_DIR
import static com.apphance.ameba.configuration.release.ReleaseConfiguration.OTA_DIR
import static org.gradle.testfixtures.ProjectBuilder.builder

class CleanReleaseTaskSpec extends Specification {

    def 'appropriate directories are cleaned'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')
        def project = builder().withProjectDir(projectDir).build()

        and:
        def rc = GroovySpy(ReleaseConfiguration)
        rc.otaDir >> project.file(OTA_DIR)
        def pc = GroovySpy(ProjectConfiguration)
        pc.project = GroovyStub(Project) {
            file(TMP_DIR) >> project.file(TMP_DIR)
        }


        and:
        def task = project.task(CleanReleaseTask.NAME, type: CleanReleaseTask) as CleanReleaseTask
        task.releaseConf = rc
        task.conf = pc

        when:
        task.clean()

        then:
        project.file(TMP_DIR).exists()
        project.file(TMP_DIR).list().size() == 0
        project.file(OTA_DIR).exists()
        project.file(OTA_DIR).list().size() == 0
    }
}
