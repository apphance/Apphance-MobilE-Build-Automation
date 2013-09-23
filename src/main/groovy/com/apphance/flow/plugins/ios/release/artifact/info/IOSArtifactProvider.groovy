package com.apphance.flow.plugins.ios.release.artifact.info

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.release.FlowArtifact

import javax.inject.Inject

class IOSArtifactProvider {

    @Inject IOSReleaseConfiguration releaseConf

    IOSSimArtifactInfo simInfo(AbstractIOSVariant v) {
        new IOSSimArtifactInfo(
                id: v.name,
                filePrefix: "$v.name-$v.fullVersionString"
        )
    }

    IOSDeviceArtifactInfo deviceInfo(AbstractIOSVariant v) {
        new IOSDeviceArtifactInfo(
                id: v.name,
                filePrefix: "$v.name-$v.fullVersionString",
                mobileprovision: v.mobileprovision.value,
                versionString: v.versionString
        )
    }

    IOSFrameworkArtifactInfo frameworkInfo(AbstractIOSVariant v) {
        new IOSFrameworkArtifactInfo(
                id: v.name,
                filePrefix: "$v.name-$v.fullVersionString",
                frameworkName: v.frameworkName.value,
                variantDir: v.tmpDir
        )
    }

    FlowArtifact xcArchive(IOSDeviceArtifactInfo info) {
        artifact('XC Archive', info, "${info.filePrefix}_xcarchive.zip")
    }

    FlowArtifact zipDistribution(IOSDeviceArtifactInfo info) {
        artifact('Distribution ZIP', info, "${info.filePrefix}.zip")
    }

    FlowArtifact dSYMZip(IOSDeviceArtifactInfo info) {
        artifact('dSYM ZIP', info, "${info.filePrefix}_dSYM.zip")
    }

    FlowArtifact ahSYM(IOSDeviceArtifactInfo info) {
        artifact('ahSYM dir', info, "${info.filePrefix}_ahSYM")
    }

    FlowArtifact ipa(IOSDeviceArtifactInfo info) {
        artifact('IPA file', info, "${info.filePrefix}.ipa")
    }

    FlowArtifact manifest(IOSDeviceArtifactInfo info) {
        artifact('Manifest file', info, 'manifest.plist')
    }

    FlowArtifact mobileprovision(IOSDeviceArtifactInfo info) {
        artifact('Mobile provision file', info, "${info.filePrefix}.mobileprovision")
    }

    FlowArtifact simulator(IOSSimArtifactInfo info, IOSFamily family) {
        artifact("Simulator build for ${family.iFormat()}", info,
                "${info.filePrefix}-${family.iFormat()}-sim-img.dmg")
    }

    FlowArtifact framework(IOSFrameworkArtifactInfo info) {
        artifact("Framework zip for $info.frameworkName", info, "${info.filePrefix}-${info.frameworkName}.zip")
    }

    private FlowArtifact artifact(String name, AbstractIOSArtifactInfo info, String suffix) {
        new FlowArtifact(
                name: name,
                url: new URL("$releaseConf.releaseUrlVersioned/$info.id/$suffix"),
                location: new File(releaseConf.releaseDir, "$info.id/$suffix")
        )
    }
}
