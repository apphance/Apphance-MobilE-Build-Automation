package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.IOSTCVariant
import com.apphance.ameba.configuration.reader.PropertyPersister
import spock.lang.Specification

class IOSTCVariantSpec extends Specification {

    def 'target and configuration is found well'() {
        given:
        def c = GroovyMock(IOSConfiguration)
        c.targetConfigurationMatrix >> [['t1', 't2', 't3'], ['c1', 'c3', 'c4']].combinations().sort()

        and:
        def pp = GroovyMock(PropertyPersister)
        pp.get(_) >> ''

        and:
        def tcVariant = new IOSTCVariant(name)
        tcVariant.conf = c
        tcVariant.propertyPersister = pp


        when:
        tcVariant.init()

        then:
        tcVariant.target == target
        tcVariant.configuration == conf

        and:
        c.targetConfigurationMatrix == [
                ['t1', 'c1'], ['t1', 'c3'], ['t1', 'c4'],
                ['t2', 'c1'], ['t2', 'c3'], ['t2', 'c4'],
                ['t3', 'c1'], ['t3', 'c3'], ['t3', 'c4'],
        ]

        where:
        name   | target | conf
        't1c3' | 't1'   | 'c3'
        't1c1' | 't1'   | 'c1'
        't3c4' | 't3'   | 'c4'
    }
}
