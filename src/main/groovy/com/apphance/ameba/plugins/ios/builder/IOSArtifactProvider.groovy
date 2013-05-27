package com.apphance.ameba.plugins.ios.builder

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE

class IOSArtifactProvider {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSVariantsConfiguration variantsConf

    IOSBuilderInfo builderInfo(AbstractIOSVariant v) {
        def bi = new IOSBuilderInfo(
                id: v.name,
                target: v.target,
                configuration: v.configuration,
                mode: v.mode.value,
                buildDir: new File(v.tmpDir, "/build/${v.configuration}-${v.mode.value == DEVICE ? 'iphoneos' : 'iphonesimulator'}"),
                fullReleaseName: "${v.name}-${v.fullVersionString}",
                filePrefix: "${v.name}-${v.fullVersionString}",
                mobileprovision: v.mobileprovision.value,
                plist: v.plist
        )
        bi
    }

    AmebaArtifact zipDistribution(IOSBuilderInfo bi) {
        artifact('Distribution ZIP', bi, "${bi.filePrefix}.zip")
    }

    AmebaArtifact dSYMZip(IOSBuilderInfo bi) {
        artifact('dSYM ZIP', bi, "${bi.filePrefix}_dSYM.zip")
    }

    AmebaArtifact ahSYM(IOSBuilderInfo bi) {
        artifact('ahSYM dir', bi, "${bi.filePrefix}_ahSYM")
    }

    AmebaArtifact ipa(IOSBuilderInfo bi) {
        artifact('IPA file', bi, "${bi.filePrefix}.ipa")
    }

    AmebaArtifact manifest(IOSBuilderInfo bi) {
        artifact('Manifest file', bi, 'manifest.plist')
    }

    AmebaArtifact mobileprovision(IOSBuilderInfo bi) {
        artifact('Mobile provision file', bi, "${bi.filePrefix}.mobileprovision")
    }

    private AmebaArtifact artifact(String name, IOSBuilderInfo bi, String suffix) {
        new AmebaArtifact(
                name: name,
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/$suffix"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/$suffix")
        )
    }

    String getFolderPrefix(IOSBuilderInfo bi) {
        "${releaseConf.projectDirName}/${variantsConf.mainVariant.fullVersionString}/${bi.target}/${bi.configuration}"
    }
}
