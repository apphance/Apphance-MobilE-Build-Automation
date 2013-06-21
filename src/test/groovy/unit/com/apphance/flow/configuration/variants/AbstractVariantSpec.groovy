package com.apphance.flow.configuration.variants

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class AbstractVariantSpec extends Specification {

    def variant = new AndroidVariantConfiguration('name')

    def 'test apphanceLibVersion validator'() {
        expect:
        variant.apphanceLibVersion.validator(correct)

        where:
        correct << ['1.8', '1.8.2', '1.9-RC1', '2.0.1.1.1']
    }

    @Unroll
    def 'test apphanceLibVersion validator #incorrect value'() {
        expect:
        !variant.apphanceLibVersion.validator(incorrect)

        where:
        incorrect << ['1.8RC', '1.9RC1', 'ver2', 'V', '1.9-RC1-M2', '1.9-RC1-RC2', '1.9--RC1', 'RC1']
    }
}
