package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import spock.lang.Specification

class IOSUnitTestConfigurationSpec extends Specification {

    def 'test getVariant'() {
        given:
        def iOSVariantsConf = new IOSVariantsConfiguration()
        def var1 = GroovyStub(AbstractIOSVariant) { getName() >> 'variantName1' }
        def var2 = GroovyStub(AbstractIOSVariant) { getName() >> 'variantName2' }
        iOSVariantsConf.@variants = [var1, var2]
        iOSVariantsConf.variantsNames = new ListStringProperty(value: ['variantName1', 'variantName2'])

        def testConf = new IOSUnitTestConfiguration()
        testConf.iosVariantsConf = iOSVariantsConf
        testConf.variant = new StringProperty(value: 'variantName1')

        expect:
        testConf.getVariant() == var1
    }
}
