package com.apphance.flow.configuration.properties

import spock.lang.Specification
import spock.lang.Unroll

import static java.lang.System.getProperties

class AbstractPropertySpec extends Specification {

    @Unroll
    def '#cls property is set correctly'() {
        when:
        AbstractProperty ap = cls.newInstance()
        ap.value = value

        then:
        ap.value == expectedValue

        where:
        cls             | value                        | expectedValue
        StringProperty  | 'sp'                         | 'sp'
        FileProperty    | properties['java.io.tmpdir'] | new File(properties['java.io.tmpdir'].toString())
        BooleanProperty | 'true'                       | true
        BooleanProperty | 'false'                      | false
        BooleanProperty | 'whatever'                   | false
        BooleanProperty | null                         | null
    }

    def 'resetValue() sets value to null'() {
        expect:
        property.value

        when:
        property.resetValue()

        then:
        !property.value

        where:
        property << [new FileProperty(value: 'some file'), new StringProperty(value: 'some string')]
    }
}
