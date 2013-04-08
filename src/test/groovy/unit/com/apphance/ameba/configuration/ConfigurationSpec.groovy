package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty
import spock.lang.Shared
import spock.lang.Specification

class ConfigurationSpec extends Specification {

    @Shared
    def androidConf = new AndroidConfiguration(* [null] * 5)

    def 'returns list of fields annotated with @AmebaProp'() {
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
        props*.name.containsAll(['android.project.name', 'android.version.code', 'android.version.string', 'android.dir.build', 'android.dir.tmp',
                'android.dir.log'])
    }

    def 'configuration name'() {
        expect: new AndroidReleaseConfiguration().nameKey == 'android.release.configuration'
    }
}
