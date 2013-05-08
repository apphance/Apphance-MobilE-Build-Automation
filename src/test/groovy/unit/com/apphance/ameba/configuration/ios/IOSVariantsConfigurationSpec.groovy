package com.apphance.ameba.configuration.ios

import com.apphance.ameba.executor.IOSExecutor
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.ameba.plugins.ios.XCodeOutputParserSpec.LIST_WITHOUT_SCHEME
import static com.apphance.ameba.plugins.ios.XCodeOutputParserSpec.getXCODE_LIST

class IOSVariantsConfigurationSpec extends Specification {

    @Unroll
    def 'test buildVariantsList #variantClass variant'() {
        given:
        def configuration = new IOSVariantsConfiguration()
        configuration.iosExecutor = Stub(IOSExecutor, { list() >> output })

        when:
        def variants = configuration.buildVariantsList()

        then:
        variants.size() == expectedSize
        variants.every { it.class == variantClass }

        where:
        expectedSize | variantClass     | output
        5            | IOSSchemeVariant | XCODE_LIST
        24           | IOSTCVariant     | LIST_WITHOUT_SCHEME
    }
}
