package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.IOSSchemeVariant
import com.apphance.ameba.configuration.ios.variants.IOSTCVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantFactory
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.reader.PropertyPersister
import spock.lang.Specification
import spock.lang.Unroll

class IOSVariantsConfigurationSpec extends Specification {

    @Unroll
    def 'test buildVariantsList #variantClass variant'() {
        given:
        def conf = GroovyMock(IOSConfiguration)
        def targets = ['Debug', 'Release', 'QAWithApphance', 'QAWithoutApphance']
        def configurations = ['Some', 'UnitTests', 'SomeWithMonkey', 'RunMonkeyTests', 'SomeSpecs', 'OtherSomeSpecs']
        conf.schemes >> schemes
        conf.targetConfigurationMatrix >> [targets, configurations].combinations()

        and:
        def vf = GroovyMock(IOSVariantFactory)
        vf.createSchemeVariant(_) >> new IOSSchemeVariant('scheme')
        vf.createTCVariant(_) >> new IOSTCVariant('tc')

        and:
        def variantsConf = new IOSVariantsConfiguration()
        variantsConf.conf = conf
        variantsConf.propertyPersister = Stub(PropertyPersister, { get(_) >> '' })
        variantsConf.variantFactory = vf

        when:
        def variants = variantsConf.buildVariantsList()

        then:
        variants.size() == expectedSize
        variants.every { it.class == variantClass }

        where:
        expectedSize | variantClass     | schemes
        5            | IOSSchemeVariant | ['Some', 'SomeWithMonkey', 'SomeSpecs', 'OtherSomeSpecs', 'RunMonkeyTests']
        24           | IOSTCVariant     | []
    }
}
