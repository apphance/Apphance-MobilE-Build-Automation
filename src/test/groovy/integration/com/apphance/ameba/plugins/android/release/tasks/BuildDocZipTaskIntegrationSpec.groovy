package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class BuildDocZipTaskIntegrationSpec extends Specification {

    def 'documentation zip is built in correct location'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')

        and:
        def project = builder().withProjectDir(projectDir).build()

        and:
        def arc = GroovyMock(AndroidReleaseConfiguration)
        def documentationZipName = "TestAndroidProject-1.0.1_42-doc.zip"
        arc.documentationZip >> new AmebaArtifact(
                name: "TestAndroidProject-doc",
                location: new File('ameba-tmp', documentationZipName))

        and:
        def task = project.task(BuildDocZipTask.NAME, type: BuildDocZipTask) as BuildDocZipTask
        task.releaseConf = arc

        when:
        task.buildDocZip()

        then:
        def f = new File(projectDir, "ameba-tmp/${documentationZipName}")
        f.exists()
        f.size() > 30000
    }

    def 'documentation zip throws exception'() {
        given:
        def project = builder().build()

        and:
        def arc = GroovyMock(AndroidReleaseConfiguration)
        arc.documentationZip >> null

        and:
        def task = project.task(BuildDocZipTask.NAME, type: BuildDocZipTask) as BuildDocZipTask
        task.releaseConf = arc

        when:
        task.buildDocZip()

        then:
        def e = thrown(NullPointerException)
        e.message == 'Documentation ZIP artifact is not configured!'
    }
}
