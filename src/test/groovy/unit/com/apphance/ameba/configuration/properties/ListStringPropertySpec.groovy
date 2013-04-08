package com.apphance.ameba.configuration.properties

import spock.lang.Specification

class ListStringPropertySpec extends Specification {

    def 'value is set correctly'() {
        given:
        def lsp = new ListStringProperty()

        when:
        lsp.value = value

        then:
        lsp.value == expectedValue

        where:
        value   | expectedValue
        null    | null
        ''      | null
        '\n'    | null
        'a,b,c' | ['a', 'b', 'c']
    }

    def 'persistent form is correct'() {
        given:
        def lsp = new ListStringProperty()

        when:
        lsp.value = value

        then:
        lsp.persistentForm() == expectedPersitentForm

        where:
        value   | expectedPersitentForm
        null    | ''
        ''      | ''
        '\n'    | ''
        'a,b,c' | 'a,b,c'
    }
}
