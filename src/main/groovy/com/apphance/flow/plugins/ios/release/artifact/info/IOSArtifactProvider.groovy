package com.apphance.flow.plugins.ios.release.artifact.info

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.plugins.release.FlowArtifact

import javax.inject.Inject

class IOSArtifactProvider {

    @Inject IOSReleaseConfiguration releaseConf

    IOSSimArtifactInfo simInfo(IOSVariant v) {
        new IOSSimArtifactInfo(
                id: v.name,
                filePrefix: "$v.name-$v.fullVersionString",
                mobileprovision: v.mobileprovision.value,
        )
    }

    IOSDeviceArtifactInfo deviceInfo(IOSVariant v) {
        new IOSDeviceArtifactInfo(
                id: v.name,
                filePrefix: "$v.name-$v.fullVersionString",
                mobileprovision: v.mobileprovision.value,
                versionString: v.versionString
        )
    }

    IOSFrameworkArtifactInfo frameworkInfo(IOSVariant v) {
        null
    }

    FlowArtifact xcArchive(IOSDeviceArtifactInfo bi) {
        artifact('XC Archive', bi, "${bi.filePrefix}_xcarchive.zip")
    }

    FlowArtifact zipDistribution(IOSDeviceArtifactInfo bi) {
        artifact('Distribution ZIP', bi, "${bi.filePrefix}.zip")
    }

    FlowArtifact dSYMZip(IOSDeviceArtifactInfo bi) {
        artifact('dSYM ZIP', bi, "${bi.filePrefix}_dSYM.zip")
    }

    FlowArtifact ahSYM(IOSDeviceArtifactInfo bi) {
        artifact('ahSYM dir', bi, "${bi.filePrefix}_ahSYM")
    }

    FlowArtifact ipa(IOSDeviceArtifactInfo bi) {
        artifact('IPA file', bi, "${bi.filePrefix}.ipa")
    }

    FlowArtifact manifest(IOSDeviceArtifactInfo bi) {
        artifact('Manifest file', bi, 'manifest.plist')
    }

    FlowArtifact mobileprovision(IOSDeviceArtifactInfo bi) {
        artifact('Mobile provision file', bi, "${bi.filePrefix}.mobileprovision")
    }

    FlowArtifact simulator(IOSSimArtifactInfo bi, IOSFamily family) {
        artifact("Simulator build ${family.iFormat()}", bi,
                "${bi.filePrefix}-${family.iFormat()}-sim-img.dmg")
    }

    private FlowArtifact artifact(String name, AbstractIOSArtifactInfo bi, String suffix) {
        new FlowArtifact(
                name: name,
                url: new URL("$releaseConf.releaseUrlVersioned/$bi.id/$suffix"),
                location: new File(releaseConf.releaseDir, "$bi.id/$suffix")
        )
    }
}
