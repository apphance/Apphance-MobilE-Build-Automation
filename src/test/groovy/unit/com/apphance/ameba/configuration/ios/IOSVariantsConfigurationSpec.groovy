package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.*
import com.apphance.ameba.configuration.properties.IOSBuildModeProperty
import com.apphance.ameba.configuration.reader.PropertyPersister
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.ameba.configuration.ios.IOSBuildMode.SIMULATOR

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

    def 'variants by mode'() {
        given:
        def vConf = GroovySpy(IOSVariantsConfiguration)
        vConf.getVariants() >> [
                GroovyStub(AbstractIOSVariant, {
                    getName() >> 'v1'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                }),
                GroovyStub(AbstractIOSVariant, {
                    getName() >> 'v2'
                    getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                })
        ]

        expect:
        vConf.deviceVariants*.name == ['v1']
        vConf.simulatorVariants*.name == ['v2']
    }
}
