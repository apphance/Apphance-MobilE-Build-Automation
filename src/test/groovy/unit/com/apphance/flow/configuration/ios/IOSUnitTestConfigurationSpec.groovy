package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import spock.lang.Specification

class IOSUnitTestConfigurationSpec extends Specification {

    def 'test getVariant'() {
        given:
        def iOSVariantsConf = GroovyMock(IOSVariantsConfiguration)
        def var1 = GroovyStub(AbstractIOSVariant) { getName() >> 'variantName1' }
        def var2 = GroovyStub(AbstractIOSVariant) { getName() >> 'variantName2' }
        iOSVariantsConf.getVariants() >> [var1, var2]
        iOSVariantsConf.getVariantsNames() >> new ListStringProperty(value: ['variantName1', 'variantName2'])

        def testConf = new IOSUnitTestConfiguration()
        testConf.iosVariantsConf = iOSVariantsConf
        testConf.variant = new StringProperty(value: 'variantName1')

        expect:
        testConf.getVariant() == var1
    }
}
