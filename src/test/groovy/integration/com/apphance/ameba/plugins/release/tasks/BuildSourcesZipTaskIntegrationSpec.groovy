package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification

import static com.apphance.ameba.configuration.AbstractConfiguration.TMP_DIR
import static org.gradle.testfixtures.ProjectBuilder.builder

class BuildSourcesZipTaskIntegrationSpec extends Specification {

    def 'sources zip is built in correct location'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')

        and:
        def project = builder().withProjectDir(projectDir).build()

        and:
        def rc = GroovyMock(AndroidReleaseConfiguration)
        def sourceZipName = "TestAndroidProject-1.0.1_42-src.zip"
        rc.sourcesZip >> new AmebaArtifact(
                name: "TestAndroidProject-src",
                location: new File(TMP_DIR, sourceZipName))

        and:
        def task = project.task(BuildSourcesZipTask.NAME, type: BuildSourcesZipTask) as BuildSourcesZipTask
        task.releaseConf = rc
        task.conf = new AndroidConfiguration(project, * [null] * 5)

        when:
        task.buildSourcesZip()

        then:
        def f = new File(projectDir, "${TMP_DIR}/${sourceZipName}")
        f.exists()
        f.size() > 30000
    }

    def 'source zip throws exception'() {
        given:
        def project = builder().build()

        and:
        def arc = GroovyMock(AndroidReleaseConfiguration)
        arc.sourcesZip >> null

        and:
        def task = project.task(BuildSourcesZipTask.NAME, type: BuildSourcesZipTask) as BuildSourcesZipTask
        task.releaseConf = arc

        when:
        task.buildSourcesZip()

        then:
        def e = thrown(NullPointerException)
        e.message == 'Sources ZIP artifact is not configured!'
    }
}
