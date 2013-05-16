package com.apphance.ameba.plugins.ios

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import spock.lang.Shared
import spock.lang.Specification

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
        builderInfo.configuration >> 'BasicConfiguration'
        builderInfo.filePrefix >> 'GradleXCode-BasicConfiguration-1.0.1_42'

        releaseConf = GroovyMock(IOSReleaseConfiguration) {
            getBaseURL() >> 'http://ota.polidea.pl'.toURL()
            getProjectDirName() >> 'TestIOSProject'
            getOtaDir() >> tmpDir
        }

        variantsConf = GroovyMock(IOSVariantsConfiguration) {
            getMainVariant() >> GroovyMock(AbstractIOSVariant) {
                getFullVersionString() >> '1.0.1_42'
            }
        }

        provider.releaseConf = releaseConf
        provider.variantsConf = variantsConf
    }

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'builder info is created correctly from variant'() {
        given:
        def variant = GroovyMock(AbstractIOSVariant)
        variant.target >> 'GradleXCode'
        variant.configuration >> 'BasicConfiguration'
        variant.tmpDir >> new File(properties['java.io.tmpdir'])
        variant.mobileprovision >> new FileProperty(value: new File('sample.mobileprovision'))
        variant.versionString >> '1.0.1'
        variant.versionCode >> '42'
        variant.fullVersionString >> '1.0.1_42'
        variant.plist >> new File('GradleXCode-Info.plist')

        and:
        def provider = new IOSArtifactProvider()

        when:
        def bi = provider.builderInfo(variant)

        then:
        bi.target == 'GradleXCode'
        bi.configuration == 'BasicConfiguration'
        bi.buildDir == new File(properties['java.io.tmpdir'], 'build')
        bi.filePrefix == 'GradleXCode-BasicConfiguration-1.0.1_42'
        bi.fullReleaseName == 'GradleXCode-BasicConfiguration-1.0.1_42'
        bi.id == 'GradleXCode-BasicConfiguration'
        bi.plist.name == 'GradleXCode-Info.plist'
        bi.mobileprovision.name == 'sample.mobileprovision'
    }

    def 'zip distribution artifact is built well'() {
        given:
        def aa = provider.zipDistribution(builderInfo)

        expect:
        aa.name == 'Distribution ZIP'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42.zip'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42.zip')
    }

    def 'dsym zip artifact is built well'() {
        given:
        def aa = provider.dSYMZip(builderInfo)

        expect:
        aa.name == 'dSYM ZIP'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42_dSYM.zip'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42_dSYM.zip')
    }

    def 'ahSYM artifact is built well'() {
        given:
        def aa = provider.ahSYM(builderInfo)

        expect:
        aa.name == 'ahSYM dir'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42_ahSYM'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42_ahSYM')
    }

    def 'ipa artifact is built well'() {
        given:
        def aa = provider.ipa(builderInfo)

        expect:
        aa.name == 'IPA file'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42.ipa'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42.ipa')
    }

    def 'manifest artifact is built well'() {
        given:
        def aa = provider.manifest(builderInfo)

        expect:
        aa.name == 'Manifest file'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/manifest.plist'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/manifest.plist')
    }

    def 'mobileprovision artifact is built well'() {
        given:
        def aa = provider.mobileprovision(builderInfo)

        expect:
        aa.name == 'Mobile provision file'
        aa.url.toString() == 'http://ota.polidea.pl/TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42.mobileprovision'
        aa.location == new File(tmpDir, 'TestIOSProject/1.0.1_42/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0.1_42.mobileprovision')
    }
}
