package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.google.common.io.Files
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir
import static java.lang.System.currentTimeMillis

class XCSchemeParserSpec extends Specification {

    @Shared
    def schemeName = 'GradleXCode'
    def conf
    def tmpDir
    def parser

    def setup() {
        tmpDir = createTempDir()

        conf = GroovyMock(IOSConfiguration)
        conf.schemesDir >> tmpDir

        parser = new XCSchemeParser()
        parser.conf = conf
    }

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'configuration for scheme is read correctly'() {
        given:
        def schemeFile = new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/xcshareddata/xcschemes/${schemeName}.xcscheme")
        Files.copy(schemeFile, new File(tmpDir, "${schemeName}.xcscheme"))

        expect:
        parser.configurationName(schemeName) == 'BasicConfiguration'
    }

    def 'blueprintIdentifier for scheme is read correctly'() {
        given:
        def schemeFile = new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/xcshareddata/xcschemes/${schemeName}.xcscheme")
        Files.copy(schemeFile, new File(tmpDir, "${schemeName}.xcscheme"))

        when:
        def id = parser.blueprintIdentifier(schemeName)

        then:
        id == 'D382B71014703FE500E9CC9B'
    }

    def 'buildableName for scheme is read correctly'() {
        given:
        def schemeFile = new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/xcshareddata/xcschemes/${schemeName}.xcscheme")
        Files.copy(schemeFile, new File(tmpDir, "${schemeName}.xcscheme"))

        expect:
        parser.buildableName(schemeName) == 'GradleXCode.app'
    }

    def 'exception is thrown when no scheme file exists'() {
        given:
        def schemeName = "RandomScheme${currentTimeMillis()}"

        when:
        parser.configurationName(schemeName)

        then:
        def e = thrown(GradleException)
        e.message == "Shemes must be shared! Invalid scheme file: ${new File(conf.schemesDir, "${schemeName}.xcscheme").absolutePath}"
    }

    def 'buildable scheme is recognized'() {
        given:
        def schemeFile = new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/xcshareddata/xcschemes/${scheme}.xcscheme")
        Files.copy(schemeFile, new File(tmpDir, "${scheme}.xcscheme"))

        expect:
        parser.isBuildable(scheme) == buildable

        where:
        scheme                      | buildable
        schemeName                  | true
        'GradleXCodeNoLaunchAction' | false
    }

}
