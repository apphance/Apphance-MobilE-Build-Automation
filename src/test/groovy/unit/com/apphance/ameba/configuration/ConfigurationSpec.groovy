package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

class ConfigurationSpec extends Specification {

    @Shared
    def androidConf = new AndroidConfiguration(ProjectBuilder.builder().build(), * [null] * 5)

    def 'return list of fields subclassed from AbstractProperty'() {
        when:
        def fields = androidConf.propertyFields

        then:
        fields.size() > 0
        fields.every { f -> (f.accessible = true) && f.get(androidConf).class.superclass == AbstractProperty }
    }

    def 'return list of properties'() {
        when:
        def props = androidConf.amebaProperties
        then:
        props*.name.containsAll(['android.project.name'])
    }

    def 'configuration name'() {
        expect: new AndroidReleaseConfiguration().enabledPropKey == 'release.configuration.enabled'
    }
}
