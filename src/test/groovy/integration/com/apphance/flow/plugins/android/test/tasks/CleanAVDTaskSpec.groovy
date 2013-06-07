package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidTestConfiguration
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class CleanAVDTaskSpec extends Specification {

    def 'avds dir is deleted'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')
        def project = builder().withProjectDir(projectDir).build()

        and:
        def atc = GroovyMock(AndroidTestConfiguration)
        atc.AVDDir >> project.file('avds')

        and:
        def task = project.task(CleanAVDTask.NAME, type: CleanAVDTask) as CleanAVDTask
        task.testConf = atc

        and:
        new File(projectDir, 'avds').mkdirs()

        when:
        task.cleanAVD()

        then:
        !(new File(projectDir, 'avds')).exists()
    }
}
