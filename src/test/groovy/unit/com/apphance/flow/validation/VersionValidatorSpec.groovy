package com.apphance.flow.validation

import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification

class VersionValidatorSpec extends Specification {

    @Shared validator = new VersionValidator()

    def 'is number'() {
        expect:
        validator.isNumber(number) == isNumber

        where:
        number | isNumber
        '12'   | true
        '  '   | false
        null   | false
        '  \n' | false
        '3145' | true
    }

    def 'has no whitespace'() {
        expect:
        validator.hasNoWhiteSpace(s) == has

        where:
        s        | has
        '12'     | true
        '  '     | false
        null     | false
        '  \n'   | false
        '3145'   | true
        '3.1.45' | true
        '3145  ' | false
    }

    def 'version code is validated correctly when empty'() {
        when:
        validator.validateVersionCode(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'version.code\' has invalid value!'

        where:
        code << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'version code is validated correctly when set'() {
        when:
        validator.validateVersionCode(code)

        then:
        noExceptionThrown()

        where:
        code << ['121', '1']
    }

    def 'version string is validated correctly when empty'() {
        when:
        validator.validateVersionString(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'version.string\' has invalid value!'

        where:
        code << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'version string is validated correctly when set'() {
        when:
        validator.validateVersionString(code)

        then:
        noExceptionThrown()

        where:
        code << ['versionString', 'version_String', 'version_String_123_4']
    }
}