package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification

class IOSTestConfigurationSpec extends Specification {

    def 'test variant'() {
        given:
        def iOSVariantsConf = GroovyMock(IOSVariantsConfiguration)
        def var1 = GroovyStub(IOSVariant) { getName() >> 'variantName1' }
        def var2 = GroovyStub(IOSVariant) { getName() >> 'variantName2' }
        iOSVariantsConf.getVariants() >> [var1, var2]
        iOSVariantsConf.getVariantsNames() >> new ListStringProperty(value: ['variantName1', 'variantName2'])

        def testConf = new IOSTestConfiguration()
        testConf.iosVariantsConf = iOSVariantsConf
        testConf.variant = new StringProperty(value: 'variantName1')

        expect:
        testConf.getVariant() == var1
    }

    def 'can be enabled according to version'() {
        given:
        def tc = new IOSTestConfiguration(executor: GroovyMock(IOSExecutor) {
            getxCodeVersion() >> xCodeVersion
            getiOSSimVersion() >> iosSimVersion
        })

        expect:
        tc.canBeEnabled() == canBeEnabled

        where:
        xCodeVersion | iosSimVersion || canBeEnabled
        '5'          | ''            || false
        '6.0.1'      | '1.5.2'       || false
        '4'          | ''            || false
        '4.6.2'      | '1.5.2'       || true
    }

    def 'disabled explained'() {
        given:
        def tc = new IOSTestConfiguration(executor: GroovyMock(IOSExecutor) {
            getxCodeVersion() >> '5.0.1'
        })

        expect:
        tc.explainDisabled() == "'iOS Unit Test Configuration' cannot be enabled because testing is supported for" +
                " xCode version lower than 5. Current version is: 5.0.1"
    }
}
