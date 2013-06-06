package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.IOSTCVariant
import com.apphance.ameba.configuration.reader.PropertyPersister
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.ameba.configuration.ios.IOSBuildMode.SIMULATOR
import static com.google.common.io.Files.createTempDir

class IOSTCVariantSpec extends Specification {

    IOSConfiguration iosConf
    def propertyPersister

    @Shared
    def tmpDir = createTempDir()

    def setup() {
        iosConf = GroovySpy(IOSConfiguration)
        iosConf.getTargets() >> ['t1', 't2', 't3']
        iosConf.getConfigurations() >> ['c1', 'c3', 'c4']
        iosConf.getTmpDir() >> tmpDir

        propertyPersister = GroovyMock(PropertyPersister)
        propertyPersister.get(_) >> ''
    }

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'target and configuration is found well'() {
        given:
        def tcVariant = new IOSTCVariant(name)
        tcVariant.conf = iosConf
        tcVariant.propertyPersister = propertyPersister

        when:
        tcVariant.init()

        then:
        tcVariant.target == target
        tcVariant.configuration == conf

        and:
        iosConf.targetConfigurationMatrix == [
                ['t1', 'c1'], ['t1', 'c3'], ['t1', 'c4'],
                ['t2', 'c1'], ['t2', 'c3'], ['t2', 'c4'],
                ['t3', 'c1'], ['t3', 'c3'], ['t3', 'c4'],
        ]

        where:
        name   | target | conf
        't1c3' | 't1'   | 'c3'
        't1c1' | 't1'   | 'c1'
        't3c4' | 't3'   | 'c4'
    }

    def 'ameba properties fields are found well'() {
        given:
        def tcVariant = new IOSTCVariant('t1c1')
        tcVariant.conf = iosConf
        tcVariant.propertyPersister = propertyPersister

        when:
        tcVariant.init()

        then:
        def fields = tcVariant.amebaProperties
        fields.size() == 6
        fields*.name.containsAll(
                [
                        'ios.variant.t1c1.mobileprovision',
                        'ios.variant.t1c1.mode',
                        'ios.variant.t1c1.bundleId',
                        'ios.variant.t1c1.apphance.mode',
                        'ios.variant.t1c1.apphance.appKey',
                        'ios.variant.t1c1.apphance.lib'
                ]
        )
    }

    def 'build cmd is constructed correctly'() {
        given:
        def tcVariant = new IOSTCVariant('t1c1')
        tcVariant.conf = iosConf
        tcVariant.propertyPersister = propertyPersister

        when:
        tcVariant.conf.sdk.value = sdk
        tcVariant.conf.simulatorSdk.value = simulatorSdk
        tcVariant.mode.value = mode
        tcVariant.init()

        then:
        tcVariant.buildCmd().join(' ') == expected

        where:
        mode      | sdk           | simulatorSdk         | expected
        SIMULATOR | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -target t1 -configuration c1 -sdk iphonesimulator6.1 -arch i386 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/t1c1/build"
        DEVICE    | ''            | 'iphonesimulator6.1' | "xcodebuild -target t1 -configuration c1 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/t1c1/build"
        DEVICE    | 'iphoneos6.1' | 'iphonesimulator6.1' | "xcodebuild -target t1 -configuration c1 -sdk iphoneos6.1 CONFIGURATION_BUILD_DIR=${tmpDir.absolutePath}/t1c1/build"

    }
}
