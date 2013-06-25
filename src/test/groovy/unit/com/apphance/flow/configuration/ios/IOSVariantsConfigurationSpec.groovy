package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSSchemeVariant
import com.apphance.flow.configuration.ios.variants.IOSTCVariant
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
        expectedSize | variantClass     | schemes
        3            | IOSSchemeVariant | ['v1', 'v2', 'v3']
        3            | IOSTCVariant     | []
    }

    @Unroll
    def 'possible variants for class #variantClass'() {
        given:
        conf.schemes >> schemes

        expect:
        variantsConf.possibleVariants.size() == expectedSize

        where:
        expectedSize | variantClass     | schemes
        3            | IOSSchemeVariant | ['v1', 'v2', 'v3']
        24           | IOSTCVariant     | []
    }

    def 'has schemes'() {
        given:
        conf.schemes >> schemes

        when:
        variantsConf.variantsNames.value.collect {
            variantsConf.hasSchemes ?
                variantsConf.variantFactory.createSchemeVariant(it)
            :
                variantsConf.variantFactory.createTCVariant(it)
        }

        then:
        variantsConf.hasSchemes == hasSchemes

        where:
        hasSchemes | schemes
        true       | ['Some', 'SomeWithMonkey']
        false      | ['', '  ']
        false      | []
    }

    def 'has schemes when some are not buildable'() {
        given:
        conf.schemes >> [schemeName]

        and:
        variantsConf.schemeParser = GroovyMock(XCSchemeParser) {
            isBuildable(schemeName) >> buildable
        }

        when:
        variantsConf.variantsNames.value.collect {
            variantsConf.hasSchemes ?
                variantsConf.variantFactory.createSchemeVariant(it)
            :
                variantsConf.variantFactory.createTCVariant(it)
        }

        then:
        variantsConf.hasSchemes == hasSchemes

        where:
        hasSchemes | schemeName | buildable
        false      | 'Some'     | false
        true       | 'Some2'    | true
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
