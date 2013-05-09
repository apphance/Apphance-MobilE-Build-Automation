package com.apphance.ameba.configuration.ios

import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSConfigurationSpec extends Specification {

    @Shared
    def p = builder().withProjectDir(new File('testProjects/ios/GradleXCode')).build()
    @Shared
    def ic = new IOSConfiguration(project: p)

    def 'possible xcodeproj dirs found'() {
        expect:
        def pv = ic.xcodeDir.possibleValues()
        pv.size() > 0
        pv.contains('GradleXCode.xcodeproj')
    }

    def 'xcodeproj dir validator works well'() {
        given:
        def validator = ic.xcodeDir.validator

        expect:
        validator(val) == expectedResult

        where:
        val                     | expectedResult
        null                    | false
        ''                      | false
        '\n  '                  | false
        'non-existing'          | false
        'GradleXCode'           | false
        'GradleXCode.xcodeproj' | true
        'ico.png'               | false

    }
}
