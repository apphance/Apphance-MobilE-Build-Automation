package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidConfigurationIntegrationSpec extends Specification {

    @Unroll
    def 'sdk jars list contains valid jars for target: #target'() {
        given:
        def project = builder().build()

        and:
        def ac = new AndroidConfiguration()
        ac.project = project
        ac.reader = new PropertyReader()
        ac.target = new StringProperty(value: target)

        expect:
        verify.call(ac.sdkJars)

        where:
        target                      | verify
        'Google Inc.:Google APIs:7' | { it*.path.any { it.endsWith('addon-google_apis-google-7/libs/maps.jar') } }
        'android-8'                 | { it*.path.any { it.endsWith('android-8/android.jar') } }
    }
}
