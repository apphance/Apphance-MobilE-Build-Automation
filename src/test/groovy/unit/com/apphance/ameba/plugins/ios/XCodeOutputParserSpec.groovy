package com.apphance.ameba.plugins.ios

import spock.lang.Shared
import spock.lang.Specification

class XCodeOutputParserSpec extends Specification {

    @Shared
    def trimmedXCodeList
    @Shared
    def trimmedShowSdkList
    @Shared
    def parser = new IOSXCodeOutputParser()

    def setupSpec() {
        trimmedXCodeList = """Information about project "Project name0":
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

    Schemes:
        Some
        SomeWithMonkey
        SomeSpecs
        OtherSomeSpecs
        RunMonkeyTests""".split("\n")*.trim()

        trimmedShowSdkList = """Mac OS X SDKs:
    Mac OS X 10.6                   -sdk macosx10.6
    Mac OS X 10.7                   -sdk macosx10.7

iOS SDKs:
    iOS 4.3                         -sdk iphoneos4.3
    iOS 5.0                         -sdk iphoneos5.0

iOS Simulator SDKs:
    Simulator - iOS 4.3             -sdk iphonesimulator4.3
    Simulator - iOS 5.0             -sdk iphonesimulator5.0

""".split("\n")*.trim()
    }

    def 'reads buildable targets'() {
        given:
        def targets = parser.readBuildableTargets(trimmedXCodeList)
        expect:
        ['Some', 'SomeWithMonkey'] == targets
    }

    def 'reads buildable configurations'() {
        given:
        def configurations = parser.readBuildableConfigurations(trimmedXCodeList)
        expect:
        ['QAWithApphance', 'QAWithoutApphance'] == configurations
    }

    def 'reads project name'() {
        given:
        def projectName = parser.readProjectName(trimmedXCodeList)
        expect:
        'Project name0' == projectName
    }

    def 'reads iphone sdks'() {
        given:
        def sdks = parser.readIphoneSdks(trimmedShowSdkList)
        expect:
        ['iphoneos', 'iphoneos4.3', 'iphoneos5.0'] == sdks
    }

    def 'reads iphone simulator sdks'() {
        given:
        def simulatorSdks = parser.readIphoneSimulatorSdks(trimmedShowSdkList)
        expect:
        ['iphonesimulator', 'iphonesimulator4.3', 'iphonesimulator5.0'] == simulatorSdks
    }

    def 'reads schemes'() {
        given:
        def schemes = parser.readSchemes(trimmedXCodeList)
        expect:
        ['Some', 'SomeWithMonkey', 'SomeSpecs', 'OtherSomeSpecs', 'RunMonkeyTests'] == schemes
    }
}
