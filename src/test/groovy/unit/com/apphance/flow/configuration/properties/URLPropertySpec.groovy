package com.apphance.flow.configuration.properties

import spock.lang.Specification

class URLPropertySpec extends Specification {

    def 'value is set correctly'() {
        given:
        def urlp = new URLProperty()

        when:
        urlp.value = ''
        then:
        urlp.value == null

        when:
        urlp.value = null
        then:
        urlp.value == null

        when:
        urlp.value = 'http://www.apphance.com'
        then:
        urlp.value == 'http://www.apphance.com'.toURL()

        when:
        urlp.value = 'malformed'
        urlp.value

        then:
        def e = thrown(MalformedURLException)
        e.message.contains('malformed')
    }
}
