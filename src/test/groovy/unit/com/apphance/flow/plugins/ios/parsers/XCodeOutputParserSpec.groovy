package com.apphance.flow.plugins.ios.parsers

import spock.lang.Shared
import spock.lang.Specification

class XCodeOutputParserSpec extends Specification {

    @Shared parser = new XCodeOutputParser()

    def static SCHEMES = """
            Schemes:
                Some
                SomeWithMonkey
                SomeSpecs
                OtherSomeSpecs
                RunMonkeyTests""".split("\n")*.trim()

    def static LIST_WITHOUT_SCHEME = """Information about project "Project name0":
            Targets:
                Some
                UnitTests
                SomeWithMonkey
                RunMonkeyTests
                SomeSpecs
                OtherSomeSpecs

            Build Configurations:
                Debug
                Release
                QAWithApphance
                QAWithoutApphance

            If no build configuration is specified "Release" is used.

        This project has a wrapper workspace:
            Some.xcodeproj/project.xcworkspace
        """.split("\n")*.trim()

    def static XCODE_LIST = LIST_WITHOUT_SCHEME + SCHEMES

    def static XCODE_SHOWSDK = """Mac OS X SDKs:
                Mac OS X 10.6                   -sdk macosx10.6
                Mac OS X 10.7                   -sdk macosx10.7

            iOS SDKs:
                iOS 4.3                         -sdk iphoneos4.3
                iOS 5.0                         -sdk iphoneos5.0

            iOS Simulator SDKs:
                Simulator - iOS 4.3             -sdk iphonesimulator4.3
                Simulator - iOS 5.0             -sdk iphonesimulator5.0

            """.split("\n")*.trim()

    def 'check xcodebuild -list output'() {
        expect:
        XCODE_LIST.size() == 27
    }

    def 'reads iphone sdks'() {
        expect:
        ['iphoneos', 'iphoneos4.3', 'iphoneos5.0'] == parser.readIphoneSdks(XCODE_SHOWSDK)
    }

    def 'reads iphone simulator sdks'() {
        expect:
        ['iphonesimulator', 'iphonesimulator4.3', 'iphonesimulator5.0'] == parser.readIphoneSimulatorSdks(XCODE_SHOWSDK)
    }

    def 'reads schemes'() {
        expect:
        ['Some', 'SomeWithMonkey', 'SomeSpecs', 'OtherSomeSpecs', 'RunMonkeyTests'] == parser.readSchemes(XCODE_LIST)
    }

    def 'parses build settings'() {
        when:
        def buildSettings = parser.parseBuildSettings(input?.split('\n')*.trim())

        then:
        verify.call(buildSettings)

        where:
        input                                                           | verify
        null                                                            | { it.isEmpty() }
        ''                                                              | { it.isEmpty() }
        new File(getClass().getResource('build_settings').toURI()).text | {
            it.size() > 0 && it.keySet().every { it2 ->
                it2.matches('([A-Z0-9a-z]+_)*([A-Z0-9a-z])+') }
        }
    }
}
