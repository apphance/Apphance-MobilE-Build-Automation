package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.release.ReleaseConfiguration
import spock.lang.Specification

import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static org.apache.commons.io.FileUtils.copyDirectory
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin([TestUtils])
class BuildSourcesZipTaskSpec extends Specification {

    def 'sources zip is built in correct location'() {
        given:
        def projectDir = temporaryDir
        copyDirectory new File(templateProjectPath), projectDir

        and:
        def project = builder().withProjectDir(projectDir).build()

        and:
        def pc = GroovySpy(ProjectConfiguration, {
            getProjectName() >> new StringProperty(value: projectName)
            getFullVersionString() >> fullVersionString
        })
        pc.project = project

        and:
        def rc = GroovySpy(ReleaseConfiguration) {
            getOtaDir() >> new File(project.rootDir, OTA_DIR)
            getReleaseDir() >> new File(OTA_DIR, "$projectName/$fullVersionString")
        }

        and:
        def task = project.task(BuildSourcesZipTask.NAME, type: BuildSourcesZipTask) as BuildSourcesZipTask
        task.releaseConf = rc
        task.conf = pc

        when:
        task.buildSourcesZip()

        then:
        def zipFile = new File(projectDir, "${rc.releaseDir}/${srcZipName}")
        zipFile.exists()
        zipFile.size() > 30000

        cleanup:
        zipFile.delete()

        where:
        templateProjectPath                  | srcZipName                            | projectName          | fullVersionString
        'testProjects/android/android-basic' | 'TestAndroidProject-1.0.1_42-src.zip' | 'TestAndroidProject' | '1.0.1_42'
        'testProjects/ios/GradleXCode'       | 'GradleXCode-1.0_32-src.zip'          | 'GradleXCode'        | '1.0_32'
    }
}