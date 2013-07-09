package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantFactory
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.reader.PropertyPersister
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import spock.lang.Specification
import spock.lang.Unroll

class IOSVariantsConfigurationSpec extends Specification {

    private IOSConfiguration conf
    private IOSVariantsConfiguration variantsConf

    def setup() {
        conf = GroovyMock(IOSConfiguration)
        def vf = GroovyMock(IOSVariantFactory)
        vf.createSchemeVariant(_) >> new IOSVariant('scheme')

        variantsConf = new IOSVariantsConfiguration()
        variantsConf.conf = conf
        variantsConf.propertyPersister = Stub(PropertyPersister, { get(_) >> '' })
        variantsConf.variantFactory = vf
        variantsConf.schemeParser = GroovyMock(XCSchemeParser) {
            isBuildable(_) >> true
        }
    }

    @Unroll
    def 'test buildVariantsList #variantClass variant'() {
        given:
        conf.schemes >> schemes
        variantsConf.variantsNames.value = ['v1', 'v2', 'v3']

        expect:
        variantsConf.variants.size() == expectedSize
        variantsConf.variants.every { it.class == variantClass }

        where:
        expectedSize | variantClass | schemes
        3            | IOSVariant   | ['v1', 'v2', 'v3']
    }

    def 'variantNames validator works'() {
        given:
        def variantsConf = new IOSVariantsConfiguration()

        expect:
        variantsConf.variantsNames.validator(input) == expected

        where:
        input        | expected
        ['v1', 'v1'] | false
        '[v1,v1]'    | false
        '[v1,v2]'    | true
        ['v1', 'v2'] | true
        []           | false
        '[]'         | false
        ['\n']       | false
    }
}
