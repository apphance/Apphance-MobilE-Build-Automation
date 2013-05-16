package com.apphance.ameba.plugins.ios

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

class IOSArtifactProvider {

    @Inject
    IOSReleaseConfiguration releaseConf
    @Inject
    IOSVariantsConfiguration variantsConf

    IOSBuilderInfo builderInfo(AbstractIOSVariant variant) {
        def bi = new IOSBuilderInfo(
                id: "${variant.target}-${variant.configuration}",
                target: variant.target,
                configuration: variant.configuration,
                buildDir: new File(variant.tmpDir, 'build'),
                fullReleaseName: "${variant.target}-${variant.configuration}-${variant.fullVersionString}",
                filePrefix: "${variant.target}-${variant.configuration}-${variant.fullVersionString}",
                mobileprovision: variant.mobileprovision.value,
                plist: variant.plist
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
