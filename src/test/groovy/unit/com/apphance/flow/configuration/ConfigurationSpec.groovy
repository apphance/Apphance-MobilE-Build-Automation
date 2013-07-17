package com.apphance.flow.configuration

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.AbstractProperty
import spock.lang.Shared
import spock.lang.Specification

class ConfigurationSpec extends Specification {

    @Shared
    def androidConf = new AndroidConfiguration()

    def 'return list of fields subclassed from AbstractProperty'() {
        when:
        def fields = androidConf.propertyFields

        then:
        fields.size() > 0
        fields.every { it.class.superclass == AbstractProperty }
    }

    def 'return list of properties'() {
        when:
        def props = androidConf.propertyFields
        then:
        props*.name.containsAll(['android.project.name'])
    }

    def 'configuration name'() {
        expect: new AndroidReleaseConfiguration().enabledPropKey == 'release.configuration.enabled'
    }
}
