package com.apphance.flow.util

import spock.lang.Specification
import spock.lang.Unroll

class VersionSpec extends Specification {

    @Unroll
    def "exception is thrown for blank version: '#version'"() {
        when:
        new Version(version)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Version can not be blank!'

        where:
        version << [null, ' ', '\t', '  ']
    }

    @Unroll
    def "exception is thrown for invalid version format: '#version'"() {
        when:
        new Version(version)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Invalid version format!'

        where:
        version << ['1,2', '1.2_0', '1.2a', '1.3-SNAPSHOT', '1.2  ']
    }

    def 'versions are compared correctly'() {
        expect:
        new Version(version1).compareTo(new Version(version2)) == expected
        where:
        version1 | version2 | expected
        '1.7'    | '1.7'    | 0
        '1.7'    | '1.7.1'  | -1
        '0.9'    | '1.0'    | -1
        '2.1'    | '2.0.9'  | 1
    }
}
