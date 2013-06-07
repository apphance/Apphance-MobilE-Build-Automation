package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static org.gradle.testfixtures.ProjectBuilder.builder

class BuildSourcesZipTaskIntegrationSpec extends Specification {

    def 'sources zip is built in correct location'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')

        and:
        def project = builder().withProjectDir(projectDir).build()

        and:
        def sourceZipName = "TestAndroidProject-1.0.1_42-src.zip"

        and:
        def ac = GroovySpy(AndroidConfiguration, {
            getProjectVersionedName() >> 'TestAndroidProject-1.0.1_42'
        })
        ac.project = project

        and:
        def task = project.task(BuildSourcesZipTask.NAME, type: BuildSourcesZipTask) as BuildSourcesZipTask
        task.releaseConf = new AndroidReleaseConfiguration()
        task.conf = ac

        when:
        task.buildSourcesZip()

        then:
        def f = new File(projectDir, "${TMP_DIR}/${sourceZipName}")
        f.exists()
        f.size() > 30000

        cleanup:
        f.delete()
    }
}
