package com.apphance.ameba.plugins.ios.parsers

import spock.lang.Shared
import spock.lang.Specification

class IOSXCodeOutputParserSpec extends Specification {

    @Shared
    def parser = new IOSXCodeOutputParser()

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

    def 'reads buildable targets'() {
        given:
        def targets = parser.readBuildableTargets(XCODE_LIST)
        expect:
        ['Some', 'SomeWithMonkey'] == targets
    }

    def 'reads buildable configurations'() {
        given:
        def configurations = parser.readBuildableConfigurations(XCODE_LIST)
        expect:
        ['QAWithApphance', 'QAWithoutApphance'] == configurations
    }

    def 'reads project name'() {
        given:
        def projectName = parser.readProjectName(XCODE_LIST)
        expect:
        'Project name0' == projectName
    }

    def 'reads iphone sdks'() {
        given:
        def sdks = parser.readIphoneSdks(XCODE_SHOWSDK)
        expect:
        ['iphoneos', 'iphoneos4.3', 'iphoneos5.0'] == sdks
    }

    def 'reads iphone simulator sdks'() {
        given:
        def simulatorSdks = parser.readIphoneSimulatorSdks(XCODE_SHOWSDK)
        expect:
        ['iphonesimulator', 'iphonesimulator4.3', 'iphonesimulator5.0'] == simulatorSdks
    }

    def 'reads schemes'() {
        given:
        def schemes = parser.readSchemes(XCODE_LIST)
        expect:
        ['Some', 'SomeWithMonkey', 'SomeSpecs', 'OtherSomeSpecs', 'RunMonkeyTests'] == schemes
    }
}
