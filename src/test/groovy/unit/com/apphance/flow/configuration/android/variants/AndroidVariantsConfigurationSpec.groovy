package com.apphance.flow.configuration.android.variants

import spock.lang.Specification

class AndroidVariantsConfigurationSpec extends Specification {

    def 'variantNames validator works'() {
        given:
        def variantsConf = new AndroidVariantsConfiguration()

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
