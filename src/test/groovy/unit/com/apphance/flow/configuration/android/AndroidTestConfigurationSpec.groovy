package com.apphance.flow.configuration.android

import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS
import static com.google.common.io.Files.createTempDir

class AndroidTestConfigurationSpec extends Specification {

    def 'android test configuration is enabled based on project type and internal field'() {
        given:
        def ptd = Mock(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration()
        ac.projectTypeDetector = ptd
        ac.project = GroovyStub(Project) {
            getRootDir() >> GroovyStub(File)
        }
        def atc = new AndroidTestConfiguration()
        atc.conf = ac
        atc.enabled = internalField

        then:
        atc.isEnabled() == enabled

        where:
        enabled | type    | internalField
        false   | IOS     | true
        false   | IOS     | false
        true    | ANDROID | true
        false   | ANDROID | false
    }

    def 'emulator port is found well'() {
        given:
        def atc = new AndroidTestConfiguration()

        expect:
        atc.emulatorPort
        atc.emulatorPort > 0
    }

    def 'testProjectPackage & testProjectName are set well'() {
        given:
        def p = Mock(Project)

        and:
        def ptd = Mock(ProjectTypeDetector) {
            detectProjectType(_) >> ANDROID
        }

        and:
        def ac = new AndroidConfiguration()
        ac.projectTypeDetector = ptd

        and:
        def amh = Mock(AndroidManifestHelper)
        amh.androidPackage(_) >> 'androidPackage'

        and:
        def abxh = Mock(AndroidBuildXmlHelper)
        abxh.projectName(_) >> 'androidName'

        and:
        def atc = new AndroidTestConfiguration()
        atc.project = p
        atc.conf = ac
        atc.manifestHelper = amh
        atc.buildXmlHelper = abxh
        atc.enabled = true

        when:
        atc.testDir.value = dir

        then:
        atc.testProjectName == projectName
        atc.testProjectPackage == packageName

        where:
        dir                          | projectName   | packageName
        'no-dir'                     | null          | null
        createTempDir().absolutePath | 'androidName' | 'androidPackage'
    }
}
