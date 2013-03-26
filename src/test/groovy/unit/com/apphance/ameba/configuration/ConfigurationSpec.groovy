package com.apphance.ameba.configuration

import spock.lang.Specification

class ConfigurationSpec extends Specification {

    def 'returns list of fields annotated with @AmebaProp'() {
        given:
        def androidConf = new AndroidConfiguration()

        when:
        def amebaProperties = androidConf.amebaProperties

        then:
        amebaProperties.size() > 0
        amebaProperties.every { f -> (f.accessible = true) && f.get(androidConf)?.class == Prop }
    }
}
