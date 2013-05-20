package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.IOSTCVariant
import com.apphance.ameba.configuration.reader.PropertyPersister
import spock.lang.Specification

class IOSTCVariantSpec extends Specification {

    def conf
    def propertyPersister

    def setup() {
        conf = GroovyMock(IOSConfiguration)
        conf.targetConfigurationMatrix >> [['t1', 't2', 't3'], ['c1', 'c3', 'c4']].combinations().sort()

        propertyPersister = GroovyMock(PropertyPersister)
        propertyPersister.get(_) >> ''
    }

    def 'target and configuration is found well'() {
        given:
        def tcVariant = new IOSTCVariant(name)
        tcVariant.conf = this.conf
        tcVariant.propertyPersister = propertyPersister

        when:
        tcVariant.init()

        then:
        tcVariant.target == target
        tcVariant.configuration == conf

        and:
        this.conf.targetConfigurationMatrix == [
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

    def 'ameba properties fields are found well'() {
        given:
        def tcVariant = new IOSTCVariant('t1c1')
        tcVariant.conf = conf
        tcVariant.propertyPersister = propertyPersister

        when:
        tcVariant.init()

        then:
        def fields = tcVariant.amebaProperties
        fields.size() == 4
        fields*.name.containsAll(
                [
                        'ios.variant.t1c1.mobileprovision',
                        'ios.variant.t1c1.apphance.mode',
                        'ios.variant.t1c1.apphance.appKey',
                        'ios.variant.t1c1.apphance.lib'
                ]
        )
    }
}
