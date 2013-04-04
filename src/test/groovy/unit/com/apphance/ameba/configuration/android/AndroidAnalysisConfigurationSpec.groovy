package com.apphance.ameba.configuration.android

import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class AndroidAnalysisConfigurationSpec extends Specification {

    def 'android analysis configuration is enabled based on project type and internal field'() {
        given:
        def p = Mock(Project)

        and:
        def ptd = Mock(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration(p, * [null] * 3, ptd)
        def aac = new AndroidAnalysisConfiguration(ac)
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
