package com.apphance.flow.plugins.ios.release.artifact.info

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.google.common.io.Files.createTempDir

class IOSArtifactProviderSpec extends Specification {

    @Shared projectName = 'TestIOS'
    @Shared fullVersionString = '1.0.1_42'
    @Shared variantName = 'V1'
    @Shared filePrefix = "$variantName-$fullVersionString"
    @Shared tmpDir = createTempDir()
    @Shared variant = GroovyMock(IOSVariant) {
        getName() >> variantName
        getFullVersionString() >> fullVersionString
        getMobileprovision() >> new FileProperty()
        getFrameworkName() >> new StringProperty(value: 'Flow-Framework')
    }
    @Shared releaseConf = GroovyMock(IOSReleaseConfiguration) {
        getReleaseUrlVersioned() >> "http://ota.polidea.pl/$projectName/$fullVersionString".toURL()
        getReleaseDir() >> new File(tmpDir, "$projectName/$fullVersionString")
    }
    @Shared provider = new IOSArtifactProvider(releaseConf: releaseConf)
    @Shared deviceInfo = provider.deviceInfo(variant)

    def cleanupSpec() {
        tmpDir.deleteDir()
    }

    def 'xcarchive zip artifact is built well'() {
        given:
        def aa = provider.xcArchive(deviceInfo)

        expect:
        aa.name == 'XC Archive'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}_xcarchive.zip"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/${filePrefix}_xcarchive.zip")
    }

    def 'zip distribution artifact is built well'() {
        given:
        def aa = provider.zipDistribution(deviceInfo)

        expect:
        aa.name == 'Distribution ZIP'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}.zip"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/${filePrefix}.zip")
    }

    def 'dsym zip artifact is built well'() {
        given:
        def aa = provider.dSYMZip(deviceInfo)

        expect:
        aa.name == 'dSYM ZIP'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}_dSYM.zip"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/${filePrefix}_dSYM.zip")
    }

    def 'ahSYM artifact is built well'() {
        given:
        def aa = provider.ahSYM(deviceInfo)

        expect:
        aa.name == 'ahSYM dir'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}_ahSYM"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/${filePrefix}_ahSYM")
    }

    def 'ipa artifact is built well'() {
        given:
        def aa = provider.ipa(deviceInfo)

        expect:
        aa.name == 'IPA file'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}.ipa"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/${filePrefix}.ipa")
    }

    def 'manifest artifact is built well'() {
        given:
        def aa = provider.manifest(deviceInfo)

        expect:
        aa.name == 'Manifest file'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/manifest.plist"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/manifest.plist")
    }

    def 'mobileprovision artifact is built well'() {
        given:
        def aa = provider.mobileprovision(deviceInfo)

        expect:
        aa.name == 'Mobile provision file'
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}.mobileprovision"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/${filePrefix}.mobileprovision")
    }

    @Unroll
    def 'simulator a for family #family is built well'() {
        given:
        def info = provider.simInfo(variant)
        def aa = provider.simulator(info, family)

        expect:
        aa.name == "Simulator build for ${family.iFormat()}"
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}-${family.iFormat()}-sim-img.dmg"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/$filePrefix-${family.iFormat()}-sim-img.dmg")

        where:
        family << IOSFamily.values()
    }

    def 'framework artifact is built'() {
        given:
        def info = provider.frameworkInfo(variant)
        def aa = provider.framework(info)

        expect:
        aa.name == "Framework zip for ${variant.frameworkName.value}"
        aa.url.toString() == "${releaseConf.releaseUrlVersioned}/${variant.name}/${filePrefix}-${variant.frameworkName.value}.zip"
        aa.location == new File(tmpDir, "$projectName/$fullVersionString/$variantName/$filePrefix-${variant.frameworkName.value}.zip")
    }
}
