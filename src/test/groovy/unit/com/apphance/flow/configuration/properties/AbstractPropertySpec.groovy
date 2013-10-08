package com.apphance.flow.configuration.properties

import org.gradle.api.GradleException
import spock.lang.Ignore
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

    //TODO: enable when all docs are written
    @Ignore
    def 'exception thrown when doc not specified'() {
        given:
        def sp = new StringProperty(name: 'prop')

        when:
        sp.doc()

        then:
        def e = thrown(GradleException)
        e.message == 'Property prop has empty doc field!'
    }

    def 'doc field returns content'() {
        given:
        def sp = new StringProperty(name: 'prop', doc: { 'has doc!' })

        when:
        def doc = sp.doc()

        then:
        noExceptionThrown()
        doc == 'has doc!'
    }
}
