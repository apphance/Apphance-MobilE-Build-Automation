package com.apphance.ameba.executor.stream

import spock.lang.Specification

class StreamAppendableSpec extends Specification {

    def 'stream appendable writes to std out'() {
        given:
        def byteStream = new ByteArrayOutputStream()

        and:
        def output = new StreamAppendable(byteStream)

        when:
        output.append('content')

        then:
        byteStream.toString() == 'content'

    }

    def 'raises exception when null stream passed'() {
        when:
        new StreamAppendable(null)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Null output stream passed'
    }
}
