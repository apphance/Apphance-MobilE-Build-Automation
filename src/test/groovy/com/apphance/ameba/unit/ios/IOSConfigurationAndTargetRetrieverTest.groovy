package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test

import com.apphance.ameba.ios.IOSConfigurationAndTargetRetriever

class IOSConfigurationAndTargetRetrieverTest {
    def trimmed
    IOSConfigurationAndTargetRetriever iosConfigurationAndTargetRetriever

    @Before
    public void setUp() {
        this.trimmed = """
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
        this.iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
    }

    @Test
    public void testStandardSelectionOfTargets() {
        assertThat(iosConfigurationAndTargetRetriever.readBuildableTargets(this.trimmed), new IsEqual(['Some', 'SomeWithMonkey']))
    }

    @Test
    public void testStandardSelectionOfConfigurations() {
        assertThat(iosConfigurationAndTargetRetriever.readBuildableConfigurations(this.trimmed), new IsEqual([
            'QAWithApphance',
            'QAWithoutApphance'
        ]))
    }
}
