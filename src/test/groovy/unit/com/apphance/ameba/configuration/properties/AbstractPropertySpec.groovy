package com.apphance.ameba.configuration.properties

import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.ameba.detection.ProjectType.ANDROID
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
        cls                 | value                        | expectedValue
        LongProperty        | '3145'                       | 3145L
        StringProperty      | 'bolo'                       | 'bolo'
        FileProperty        | properties['java.io.tmpdir'] | new File(properties['java.io.tmpdir'].toString())
        ProjectTypeProperty | 'ANDROID'                    | ANDROID
    }
}
