package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test

import com.apphance.ameba.ios.IOSXCodeOutputParser

class XCodeOutputParserTest {
    def trimmedXCodeList
    def trimmedShowSdkList

    @Before
    public void setUp() {
        this.trimmedXCodeList = """Information about project "Project name0":
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

        this.trimmedShowSdkList = """Mac OS X SDKs:
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

    @Test
    public void testStandardSelectionOfTargets() {
        assertThat(IOSXCodeOutputParser.readBuildableTargets(this.trimmedXCodeList), new IsEqual(['Some', 'SomeWithMonkey']))
    }

    @Test
    public void testStandardSelectionOfConfigurations() {
        assertThat(IOSXCodeOutputParser.readBuildableConfigurations(this.trimmedXCodeList), new IsEqual([
            'QAWithApphance',
            'QAWithoutApphance'
        ]))
    }

    @Test
    public void testProjectName() {
        assertThat(IOSXCodeOutputParser.readProjectName(this.trimmedXCodeList), new IsEqual('Project name0'))
    }

    @Test
    public void testIphoneSDKS() {
        assertThat(IOSXCodeOutputParser.readIphoneSdks(this.trimmedShowSdkList),
                new IsEqual([
                    'iphoneos',
                    'iphoneos4.3',
                    'iphoneos5.0'
                ]))
    }

    @Test
    public void testIphoneSimulatorSDKS() {
        assertThat(IOSXCodeOutputParser.readIphoneSimulatorSdks(this.trimmedShowSdkList),
                new IsEqual([
                    'iphonesimulator',
                    'iphonesimulator4.3',
                    'iphonesimulator5.0'
                ]))
    }
}
