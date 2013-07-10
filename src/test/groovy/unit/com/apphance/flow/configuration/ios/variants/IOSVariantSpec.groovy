package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.google.common.io.Files.createTempDir

class IOSVariantSpec extends Specification {

    @Shared
    def tmpDir = createTempDir()

    @Unroll
    def 'apphance enabled depending for #mode'() {
        given:
        def conf = new IOSVariant('name')
        conf.mode.value = mode

        and:
        conf.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getEnabled() >> true
        }

        expect:
        conf.apphanceEnabled == expected

        where:
        mode      | expected
        DEVICE    | true
        SIMULATOR | false
    }

    def 'apphance lib dependency is constructed correctly'() {
        given:
        def variant = GroovySpy(IOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.2')
            getTarget() >> 't'
            getBuildConfiguration() >> 'c'
            getArchiveConfiguration() >> 'a'
        }
        variant.executor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> ['ARCHS': 'armv6 armv7']
        }
        variant.apphanceArtifactory = GroovyMock(ApphanceArtifactory) {
            iOSArchs(_) >> ['armv6', 'armv7']
        }

        expect:
        variant.apphanceDependencyArch == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'armv7'
        SILENT       | 'armv7'
        PROD         | 'armv7'
    }

    def 'build cmd is constructed correctly'() {
        given:
        def iosConf = GroovySpy(IOSConfiguration)
        iosConf.getTmpDir() >> tmpDir

        and:
        def schemeParser = GroovyStub(XCSchemeParser) {
            configuration(_, _) >> 'c1'
        }

        and:
        def sVariant = new IOSVariant('scheme1')
        sVariant.conf = iosConf
        sVariant.schemeParser = schemeParser

        when:
        sVariant.conf.sdk.value = sdk
        sVariant.conf.simulatorSdk.value = simulatorSdk
        sVariant.mode.value = mode

        then:
        sVariant.buildCmd.join(' ') == expected

        cleanup:
        tmpDir.deleteDir()

        where:
        mode      | sdk           | simulatorSdk         | expected
        SIMULATOR | ''            | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build clean build"
        SIMULATOR | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build clean build"
        DEVICE    | ''            | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build clean build"
        DEVICE    | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -sdk iphoneos6.1 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build clean build"
    }
}
