package com.apphance.flow.validation

import com.apphance.flow.configuration.properties.AbstractProperty
import com.apphance.flow.configuration.properties.StringProperty
import spock.lang.Shared
import spock.lang.Specification

import static org.apache.commons.lang.StringUtils.isNotEmpty

class PropertyValidatorSpec extends Specification {

    @Shared validator = new PropertyValidator()

    def 'property is validated as expected'() {
        expect:
        expected == validator.validateCondition(condition, message)

        where:
        expected | condition               | message
        ''       | isNotEmpty('not-empty') | 'not-empty'
        'empty'  | isNotEmpty(null)        | 'empty'
    }

    def 'correct result when exception is checked'() {
        expect:
        expected == validator.throwsException(c)

        where:
        expected | c
        true     | { throw new Exception() }
        false    | { '' }
    }

    def 'collection of properties is validated'() {
        expect:
        expected == validator.validateProperties(props as AbstractProperty[])

        where:
        expected                                                                    | props
        []                                                                          | [new StringProperty(value: 'value', validator: { isNotEmpty(it) }), new StringProperty(value: '2', validator: { it.matches('\\d+') })]
        ['Incorrect value null of a property', 'Incorrect value abc of b property'] | [new StringProperty(name: 'a', value: '', validator: { isNotEmpty(it) }), new StringProperty(name: 'b', value: 'abc', validator: { it.matches('\\d+') })]
    }
}
