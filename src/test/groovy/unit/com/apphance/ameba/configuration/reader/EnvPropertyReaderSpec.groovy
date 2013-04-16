package com.apphance.ameba.configuration.reader

import spock.lang.Specification

class EnvPropertyReaderSpec extends Specification {

    def 'exception thrown when invalid name passed'() {
        given:
        def epr = new EnvPropertyReader()

        when:
        epr.readProperty(name)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Null or empty property name passed'

        where:
        name << ['', '   ', null, '\t  ']
    }

    def 'correct property is returned'() {
        given:
        def epr = new EnvPropertyReader()

        and: 'clear settings'
        System.setProperty(name, '')

        when:
        system.each {
            System.setProperty(it.key, it.value)

        }
        def p = epr.readProperty(name)

        then:
        p == expectedP

        where:
        name        | expectedP    | system
        'some.prop' | null         | [:]
        'some.prop' | 'some.value' | ['some.prop': 'some.value']
    }
}


