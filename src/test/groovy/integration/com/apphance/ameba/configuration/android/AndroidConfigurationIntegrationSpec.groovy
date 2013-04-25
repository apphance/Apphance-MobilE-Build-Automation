package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidConfigurationIntegrationSpec extends Specification {

    def 'sdk jars list contains maps.jar'() {
        given:
        def project = builder().build()

        and:
        def ac = new AndroidConfiguration(project, * [null] * 4, new PropertyReader())
        ac.target = new StringProperty(value: target)

        expect:
        verify.call(ac.sdkJars)

        where:
        target                       | verify
        'Google Inc.:Google APIs:17' | { it*.path.any { it.endsWith('addon-google_apis-google-17/libs/maps.jar') } }
        'android-8'                  | { it*.path.any { it.endsWith('android-8/android.jar') } }
    }
}
