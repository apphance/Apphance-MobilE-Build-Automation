package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.*
import com.apphance.flow.configuration.reader.PropertyPersister
import spock.lang.Specification
import spock.lang.Unroll

class IOSVariantsConfigurationSpec extends Specification {

    private IOSConfiguration conf
    private IOSVariantsConfiguration variantsConf

    def setup() {
        conf = GroovyMock(IOSConfiguration)
        def targets = ['Debug', 'Release', 'QAWithApphance', 'QAWithoutApphance']
        def configurations = ['Some', 'UnitTests', 'SomeWithMonkey', 'RunMonkeyTests', 'SomeSpecs', 'OtherSomeSpecs']

        conf.targetConfigurationMatrix >> [targets, configurations].combinations()

        def vf = GroovyMock(IOSVariantFactory)
        vf.createSchemeVariant(_) >> new IOSSchemeVariant('scheme')
        vf.createTCVariant(_) >> new IOSTCVariant('tc')

        variantsConf = new IOSVariantsConfiguration()
        variantsConf.conf = conf
        variantsConf.propertyPersister = Stub(PropertyPersister, { get(_) >> '' })
        variantsConf.variantFactory = vf
    }

    @Unroll
    def 'test buildVariantsList #variantClass variant'() {
        given:
        conf.schemes >> schemas

        when:
        def variants = variantsConf.buildVariantsList()

        then:
        variants.size() == expectedSize
        variants.every { it.class == variantClass }

        where:
        expectedSize | variantClass     | schemas
        5            | IOSSchemeVariant | ['Some', 'SomeWithMonkey', 'SomeSpecs', 'OtherSomeSpecs', 'RunMonkeyTests']
        24           | IOSTCVariant     | []
    }

    def 'has schemes'() {
        given:
        conf.schemes >> schemes

        when:
        def variants = variantsConf.buildVariantsList()

        then:
        variantsConf.hasSchemes() == hasSchemes

        where:
        hasSchemes | schemes
        true       | ['Some', 'SomeWithMonkey']
        false      | ['', '  ']
        false      | []
    }
}
