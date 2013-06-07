package com.apphance.flow.plugins.ios.builder

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.release.AmebaArtifact

import javax.inject.Inject

class IOSArtifactProvider {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSVariantsConfiguration variantsConf

    IOSBuilderInfo builderInfo(AbstractIOSVariant v) {
        def bi = new IOSBuilderInfo(
                id: v.name,
                buildableName: v.buildableName,
                target: v.target,
                configuration: v.configuration,
                mode: v.mode.value,
                buildDir: v.buildDir,
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