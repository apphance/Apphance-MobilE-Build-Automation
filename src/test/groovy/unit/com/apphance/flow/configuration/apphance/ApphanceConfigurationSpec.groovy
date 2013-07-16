package com.apphance.flow.configuration.apphance

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.detection.project.ProjectTypeDetector
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS

class ApphanceConfigurationSpec extends Specification {

    def 'apphance configuration is enabled based on project type and internal field'() {
        given:
        def ptd = Mock(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration()
        ac.projectTypeDetector = ptd
        ac.project = GroovyStub(Project) {
            getRootDir() >> GroovyStub(File)
        }
        def aac = new ApphanceConfiguration()
        aac.conf = ac
        aac.enabled = internalField

        then:
        aac.isEnabled() == enabled

        where:
        enabled | type    | internalField
        false   | IOS     | true
        false   | IOS     | false
        true    | ANDROID | true
        false   | ANDROID | false
    }
}
