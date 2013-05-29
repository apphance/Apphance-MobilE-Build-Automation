package com.apphance.ameba.configuration.ios.variants

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.plugins.ios.parsers.XCSchemeParser
import com.google.common.io.Files
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.ameba.configuration.ios.IOSBuildMode.SIMULATOR

class IOSSchemeVariantSpec extends Specification {

    @Shared
    def tmpDir = Files.createTempDir()

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'build cmd is constructed correctly'() {
        given:
        def iosConf = GroovySpy(IOSConfiguration)
        iosConf.getTargets() >> ['t1', 't2', 't3']
        iosConf.getConfigurations() >> ['c1', 'c3', 'c4']
        iosConf.getTmpDir() >> tmpDir


        and:
        def schemeParser = GroovyStub(XCSchemeParser) {
            configurationName(_) >> 'c1'
        }

        and:
        def sVariant = new IOSSchemeVariant('scheme1')
        sVariant.conf = iosConf
        sVariant.schemeParser = schemeParser

        when:
        sVariant.conf.sdk.value = sdk
        sVariant.conf.simulatorSdk.value = simulatorSdk
        sVariant.mode.value = mode

        then:
        sVariant.buildCmd().join(' ') == expected

        where:
        mode      | sdk           | simulatorSdk         | expected
        SIMULATOR | ''            | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -configuration c1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/scheme1/build"
        SIMULATOR | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -configuration c1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/scheme1/build"
        DEVICE    | ''            | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -configuration c1 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/scheme1/build"
        DEVICE    | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -configuration c1 -sdk iphoneos6.1 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/scheme1/build"
    }
}