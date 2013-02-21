package com.apphance.ameba.executor.stream

import spock.lang.Specification

class StringBuilderAppendableSpec extends Specification {

    def 'string builder appendable writes to string builder'() {
        given:
        def stringBuilder = new StringBuilder()

        and:
        def stringBuilderAppendable = new StringBuilderAppendable(stringBuilder)

        when:
        stringBuilderAppendable.append('content')

        then:
        stringBuilder.toString() == 'content'
    }

    def 'raises exception when null string builder passed'() {
        when:
        new StringBuilderAppendable(null)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Null string builder passed'
    }
}
