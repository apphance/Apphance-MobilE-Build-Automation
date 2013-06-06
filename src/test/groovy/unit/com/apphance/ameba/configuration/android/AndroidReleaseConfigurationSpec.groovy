package com.apphance.ameba.configuration.android

import spock.lang.Specification

import static com.apphance.ameba.configuration.android.AndroidReleaseConfiguration.getDRAWABLE_DIR_PATTERN

class AndroidReleaseConfigurationSpec extends Specification {

    def 'DRAWABLE_DIR_PATTERN'() {
        expect:
        (input ==~ DRAWABLE_DIR_PATTERN) == result

        where:
        input            | result
        'drawable-ldpi'  | true
        'drawable-mdpi'  | true
        'drawable-hdpi'  | true
        'drawable-xhdpi' | true
        'drawable'       | true
        'drawable-'      | false
        'drawable-abc'   | false
        'abc'            | false
    }
}
