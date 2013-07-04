package com.apphance.flow.plugins.ios.parsers

import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir

class XCSchemeParserSpec extends Specification {

    @Shared
    def schemeName = 'GradleXCode'
    @Shared
    def schemeFile = new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/xcshareddata/xcschemes/${schemeName}.xcscheme")
    def tmpDir
    def parser = new XCSchemeParser()

    def setup() {
        tmpDir = createTempDir()
        copy(schemeFile, new File(tmpDir, "${schemeName}.xcscheme"))
    }

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'configuration for scheme is read correctly'() {
        expect:
        parser.configurationName(schemeFile) == 'BasicConfiguration'
    }

    def 'blueprintIdentifier for scheme is read correctly'() {
        expect:
        'D382B71014703FE500E9CC9B' == parser.blueprintIdentifier(schemeFile)
    }

    def 'buildableName for scheme is read correctly'() {
        expect:
        parser.buildableName(schemeFile) == 'GradleXCode.app'
    }

    def 'exception is thrown when no scheme file exists'() {
        given:
        def schemeFile = new File('random')

        when:
        parser.configurationName(schemeFile)

        then:
        def e = thrown(GradleException)
        e.message == "Shemes must be shared! Invalid scheme file: ${schemeFile.absolutePath}"
    }

    @Unroll
    def 'buildable scheme is recognized for scheme #scheme'() {
        expect:
        def file = new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/xcshareddata/xcschemes/${scheme}.xcscheme")
        parser.isBuildable(file) == buildable

        where:
        scheme                      | buildable
        schemeName                  | true
        'GradleXCodeNoLaunchAction' | false
    }
}
