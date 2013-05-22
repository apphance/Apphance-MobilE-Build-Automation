package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import spock.lang.Specification

import static com.apphance.ameba.configuration.release.ReleaseConfiguration.OTA_DIR
import static org.gradle.testfixtures.ProjectBuilder.builder

class CleanReleaseTaskSpec extends Specification {

    def 'appropriate directories are cleaned'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')
        def project = builder().withProjectDir(projectDir).build()

        and:
        def rc = GroovyMock(ReleaseConfiguration)
        rc.otaDir >> project.file(OTA_DIR)
        def pc = GroovyMock(ProjectConfiguration)
        pc.tmpDir >> project.file('ameba-tmp')

        and:
        def task = project.task(CleanReleaseTask.NAME, type: CleanReleaseTask) as CleanReleaseTask
        task.releaseConf = rc
        task.conf = pc

        when:
        task.clean()

        then:
        project.file('ameba-tmp').exists()
        project.file('ameba-tmp').list().size() == 0
        project.file(OTA_DIR).exists()
        project.file(OTA_DIR).list().size() == 0
    }
}
