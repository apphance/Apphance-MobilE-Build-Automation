package com.apphance.flow.configuration.reader

import spock.lang.Specification

class PropertyReaderSpec extends Specification {

    def 'exception thrown when invalid name passed'() {
        given:
        def epr = new PropertyReader()

        when:
        epr.systemProperty(name)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Null or empty property name passed'

        where:
        name << ['', '   ', null, '\t  ']
    }

    def 'correct property is returned'() {
        given:
        def epr = new PropertyReader()

        and: 'clear settings'
        System.clearProperty(name)

        when:
        system.each {
            System.setProperty(it.key, it.value)
        }

        def p = epr.systemProperty(name)

        then:
        p == expectedP

        where:
        name        | expectedP    | system
        'some.prop' | null         | [:]
        'some.prop' | 'some.value' | ['some.prop': 'some.value']
    }
}


