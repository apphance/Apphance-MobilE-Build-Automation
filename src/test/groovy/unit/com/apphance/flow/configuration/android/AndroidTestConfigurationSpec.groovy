package com.apphance.flow.configuration.android

import com.apphance.flow.detection.project.ProjectTypeDetector
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS

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
}
