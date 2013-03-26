package com.apphance.ameba.configuration

import spock.lang.Shared
import spock.lang.Specification

class ConfigurationSpec extends Specification {

    @Shared def androidConf = new AndroidConfiguration()

    def 'returns list of fields annotated with @AmebaProp'() {
        when:
        def fields = androidConf.propertyFields

        then:
        fields.size() > 0
        fields.every { f -> (f.accessible = true) && f.get(androidConf)?.class == Prop }
    }

    def 'return list of properties'() {
        when: def props = androidConf.amebaProperties
        then: props*.name == ['android.sdk.dir', 'android.min.sdk.target.name']
    }
}
