package com.apphance.flow.plugins.ios.release.artifact

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactInfo
import com.apphance.flow.plugins.release.FlowArtifact

import javax.inject.Inject

import static java.io.File.separator

class IOSArtifactProvider {

    @Inject IOSReleaseConfiguration releaseConf

    IOSArtifactInfo builderInfo(IOSVariant v) {
        new IOSArtifactInfo(
                id: v.name,
                mode: v.mode.value,
                filePrefix: "$v.name-$v.fullVersionString",
                mobileprovision: v.mobileprovision.value,
                versionString: v.versionString
        )
    }

    FlowArtifact xcArchive(IOSArtifactInfo bi) {
        artifact('XC Archive', bi, "${bi.filePrefix}_xcarchive.zip")
    }

    FlowArtifact zipDistribution(IOSArtifactInfo bi) {
        artifact('Distribution ZIP', bi, "${bi.filePrefix}.zip")
    }

    FlowArtifact dSYMZip(IOSArtifactInfo bi) {
        artifact('dSYM ZIP', bi, "${bi.filePrefix}_dSYM.zip")
    }

    FlowArtifact ahSYM(IOSArtifactInfo bi) {
        artifact('ahSYM dir', bi, "${bi.filePrefix}_ahSYM")
    }

    FlowArtifact ipa(IOSArtifactInfo bi) {
        artifact('IPA file', bi, "${bi.filePrefix}.ipa")
    }

    FlowArtifact manifest(IOSArtifactInfo bi) {
        artifact('Manifest file', bi, 'manifest.plist')
    }

    FlowArtifact mobileprovision(IOSArtifactInfo bi) {
        artifact('Mobile provision file', bi, "${bi.filePrefix}.mobileprovision")
    }

    FlowArtifact simulator(IOSArtifactInfo bi, IOSFamily family) {
        artifact("Simulator build ${family.iFormat()}", bi,
                "${bi.filePrefix}-${family.iFormat()}-sim-img.dmg")
    }

    private FlowArtifact artifact(String name, IOSArtifactInfo bi, String suffix) {
        new FlowArtifact(
                name: name,
                url: new URL("$releaseConf.releaseUrlVersioned$separator$bi.id$separator$suffix"),
                location: new File(releaseConf.releaseDir, "$bi.id/$suffix")
        )
    }
}
