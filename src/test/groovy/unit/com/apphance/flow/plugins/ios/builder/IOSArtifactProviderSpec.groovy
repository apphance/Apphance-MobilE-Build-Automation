package com.apphance.flow.plugins.ios.builder

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.google.common.io.Files.createTempDir
import static java.lang.System.getProperties

class IOSArtifactProviderSpec extends Specification {

    @Shared
    def builderInfo
    @Shared
    def releaseConf
    @Shared
    def tmpDir = createTempDir()
    @Shared
    def variantsConf
    @Shared
    def provider = new IOSArtifactProvider()

    def setup() {
        builderInfo = GroovyMock(IOSBuilderInfo)
        builderInfo.target >> 'GradleXCode'
        builderInfo.filePrefix >> 'GradleXCode-BasicConfiguration-1.0.1_42'
        builderInfo.id >> 'VariantName'

        variantsConf = GroovyMock(IOSVariantsConfiguration) {
            getMainVariant() >> GroovyMock(IOSVariant) {
                getFullVersionString() >> '1.0.1_42'
            }
        }
        provider.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            getReleaseUrlVersioned() >> "http://ota.polidea.pl/TestIOSProject/1.0.1_42".toURL()
            getReleaseDir() >> new File(tmpDir, "TestIOSProject/1.0.1_42")
        }
    }

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'builder info is created correctly from variant'() {
        given:
        def variant = GroovyMock(IOSVariant)
        variant.target >> 'GradleXCode'
        variant.buildConfiguration >> 'BasicConfiguration'
        variant.tmpDir >> new File(properties['java.io.tmpdir'])
        variant.mobileprovision >> new FileProperty(value: new File('sample.mobileprovision'))
        variant.versionString >> '1.0.1'
        variant.versionCode >> '42'
        variant.fullVersionString >> '1.0.1_42'
        variant.mode >> new IOSBuildModeProperty(value: DEVICE)
        variant.name >> 'V1'

        and:
        def provider = new IOSArtifactProvider()

        when:
        def bi = provider.builderInfo(variant)

        then:
        bi.target == 'GradleXCode'
        bi.filePrefix == 'V1-1.0.1_42'
        bi.id == 'V1'
        bi.mobileprovision.name == 'sample.mobileprovision'
        bi.mode == DEVICE
    }

    def 'xcarchive zip artifact is built well'() {
        given:
        def aa = provider.xcArchive(builderInfo)

        expect:
        aa.name == 'XC Archive'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42_xcarchive.zip'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42_xcarchive.zip')
    }

    def 'zip distribution artifact is built well'() {
        given:
        def aa = provider.zipDistribution(builderInfo)

        expect:
        aa.name == 'Distribution ZIP'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42.zip'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42.zip')
    }

    def 'dsym zip artifact is built well'() {
        given:
        def aa = provider.dSYMZip(builderInfo)

        expect:
        aa.name == 'dSYM ZIP'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42_dSYM.zip'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42_dSYM.zip')
    }

    def 'ahSYM artifact is built well'() {
        given:
        def aa = provider.ahSYM(builderInfo)

        expect:
        aa.name == 'ahSYM dir'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42_ahSYM'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42_ahSYM')
    }

    def 'ipa artifact is built well'() {
        given:
        def aa = provider.ipa(builderInfo)

        expect:
        aa.name == 'IPA file'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42.ipa'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42.ipa')
    }

    def 'manifest artifact is built well'() {
        given:
        def aa = provider.manifest(builderInfo)

        expect:
        aa.name == 'Manifest file'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/manifest.plist'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/manifest.plist')
    }

    def 'mobileprovision artifact is built well'() {
        given:
        def aa = provider.mobileprovision(builderInfo)

        expect:
        aa.name == 'Mobile provision file'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42.mobileprovision'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/VariantName/GradleXCode-BasicConfiguration-1.0.1_42.mobileprovision')
    }
}
