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

    def 'mutual exclusion of release configuration and jar library configuration'() {
        given:
        def releaseConf = new AndroidReleaseConfiguration()
        releaseConf.jarLibraryConf = GroovyStub(AndroidJarLibraryConfiguration)
        releaseConf.jarLibraryConf.enabled >> jarEnabled

        expect:
        releaseConf.canBeEnabled() ^ jarEnabled

        where:
        jarEnabled << [true, false]
    }
}
