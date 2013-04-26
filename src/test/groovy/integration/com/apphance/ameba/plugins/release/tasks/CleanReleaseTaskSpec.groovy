package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class CleanReleaseTaskSpec extends Specification {

    def 'appropriate directories are cleaned'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')
        def project = builder().withProjectDir(projectDir).build()

        and:
        def rc = GroovyMock(ReleaseConfiguration)
        rc.otaDir >> project.file('ameba-ota')
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
        project.file('ameba-ota').exists()
        project.file('ameba-ota').list().size() == 0
    }
}
