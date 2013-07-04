package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PlistParser
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
            getConfiguration() >> 'c'
        }
        variant.executor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> ['ARCHS': 'armv6 armv7']
        }
        variant.apphanceArtifactory = GroovyMock(ApphanceArtifactory) {
            iOSArchs(_) >> ['armv6', 'armv7']
        }

        expect:
        variant.apphanceDependencyArch() == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'armv7'
        SILENT       | 'armv7'
        PROD         | 'armv7'
    }

    def 'validates version code and version string when empty'() {
        given:
        def variant = GroovySpy(IOSVariant) {
            getConf() >> GroovyMock(IOSConfiguration) {
                getExtVersionCode() >> versionCode
            }
            getPlist() >> GroovyMock(File)
            getTarget() >> ''
            getConfiguration() >> ''
        }
        variant.apphanceConf = GroovyMock(ApphanceConfiguration) {
            isEnabled() >> false
        }
        variant.plistParser = GroovyMock(PlistParser) {
            evaluate(_, _, _) >> ''
            versionCode(_) >> ''
        }

        when:
        def errors = variant.verify()

        then:
        noExceptionThrown()

        and:
        errors.size() == expectedSize
        verifyErrors.call(errors)

        where:
        versionCode | expectedSize | verifyErrors
        ''          | 2            | {
            it.find { m -> m.contains("Property versionCode must have numerical value!") } && it.find { m ->
                m.contains("Property versionString must not have whitespace characters!")
            }
        }
        '3145'      | 1            | {
            it.find { m ->
                m.contains("Property versionString must not have whitespace characters!")
            }
        }
    }

    def 'build cmd is constructed correctly'() {
        given:
        def iosConf = GroovySpy(IOSConfiguration)
        iosConf.getTmpDir() >> tmpDir

        and:
        def schemeParser = GroovyStub(XCSchemeParser) {
            configurationName(_) >> 'c1'
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
        SIMULATOR | ''            | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build"
        SIMULATOR | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build"
        DEVICE    | ''            | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build"
        DEVICE    | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -scheme scheme1 -sdk iphoneos6.1 CONFIGURATION_BUILD_DIR=$tmpDir.absolutePath/scheme1/build"
    }
}
