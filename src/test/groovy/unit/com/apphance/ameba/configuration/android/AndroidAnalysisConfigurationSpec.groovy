package com.apphance.ameba.configuration.android

import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class AndroidAnalysisConfigurationSpec extends Specification {

    def 'configuration is enabled based on project type and internal field'() {
        given:
        def ptd = GroovyStub(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration()
        ac.projectTypeDetector = ptd
        ac.project = GroovyStub(Project) {
            getRootDir() >> GroovyStub(File)
        }
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

    def 'configuration is verified properly'() {
        given:
        def aac = new AndroidAnalysisConfiguration(null)

        when:
        aac.analysisConfigUrl.value = analysisURL
        def errors = aac.verify()

        then:
        validator.call(errors)

        where:
        analysisURL             | validator
        'http://ota.polidea.pl' | { it.size() == 0 }
        null                    | { it.size() == 0 }
        'invalid'               | { it.size() == 1 && it[0] == 'Property \'android.analysis.config.url\' is not valid! Should be valid URL address!' }


    }
}
