package com.apphance.flow.configuration.properties

import spock.lang.Specification

class BooleanPropertySpec extends Specification {

    def 'default value evaluation'() {
        expect:
        new BooleanProperty(defaultValue: df).defaultValue() == expected

        where:
        expected | df
        null     | { null }
        true     | { true }
        false    | { false }
    }

    def 'exception when bad value passed'() {
        when:
        new BooleanProperty(name: 'p', value: 'invalid')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Invalid boolean value (invalid) of property p!'
    }

    def 'property value is set correctly'() {
        expect:
        new BooleanProperty(value: value).value == expectedValue

        where:
        value   | expectedValue
        'true'  | true
        'false' | false
        null    | null
        ''      | null
    }
}
