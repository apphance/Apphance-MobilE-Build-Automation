package com.apphance.flow.configuration.properties

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
        value       | expectedValue
        null        | null
        ''          | null
        '\n'        | null
        'a,b,c'     | ['a', 'b', 'c']
        '[a,b  ,c]' | ['a', 'b', 'c']
    }

    def 'persistent form is correct'() {
        given:
        def lsp = new ListStringProperty()

        when:
        lsp.value = value

        then:
        lsp.persistentForm() == expectedPersitentForm

        where:
        value     | expectedPersitentForm
        null      | ''
        ''        | ''
        '\n'      | ''
        '[a,b,c]' | 'a,b,c'
        'a,b,c'   | 'a,b,c'
    }

    def 'list is made unique'() {
        given:
        def list = new ListStringProperty(name: 'name of the list', value: value)

        when:
        list.makeUnique()

        then:
        expected == list.value

        where:
        value | expected
        '1,1' | ['1']
        '1,2' | ['1', '2']
        null  | null
        ''    | null
    }
}
