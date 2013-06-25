package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR

class AbstractIOSVariantSpec extends Specification {

    @Unroll
    def 'apphance enabled depending for #mode'() {
        given:
        def conf = new IOSSchemeVariant('name')
        conf.mode.value = mode

        and:
        conf.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getEnabled() >> true
        }

        expect:
        conf.apphanceEnabled == expected

        where:
        mode      | expected
        DEVICE    | true
        SIMULATOR | false
    }
}
