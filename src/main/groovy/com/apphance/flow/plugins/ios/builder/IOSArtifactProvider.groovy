package com.apphance.flow.plugins.ios.builder

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.release.FlowArtifact

import javax.inject.Inject

class IOSArtifactProvider {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSVariantsConfiguration variantsConf

    IOSBuilderInfo builderInfo(IOSVariant v) {
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
                plist: v.plist,
                versionString: v.versionString
        )
        bi
    }

    FlowArtifact zipDistribution(IOSBuilderInfo bi) {
        artifact('Distribution ZIP', bi, "${bi.filePrefix}.zip")
    }

    FlowArtifact dSYMZip(IOSBuilderInfo bi) {
        artifact('dSYM ZIP', bi, "${bi.filePrefix}_dSYM.zip")
    }

    FlowArtifact ahSYM(IOSBuilderInfo bi) {
        artifact('ahSYM dir', bi, "${bi.filePrefix}_ahSYM")
    }

    FlowArtifact ipa(IOSBuilderInfo bi) {
        artifact('IPA file', bi, "${bi.filePrefix}.ipa")
    }

    FlowArtifact manifest(IOSBuilderInfo bi) {
        artifact('Manifest file', bi, 'manifest.plist')
    }

    FlowArtifact mobileprovision(IOSBuilderInfo bi) {
        artifact('Mobile provision file', bi, "${bi.filePrefix}.mobileprovision")
    }

    private FlowArtifact artifact(String name, IOSBuilderInfo bi, String suffix) {
        new FlowArtifact(
                name: name,
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/$suffix"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/$suffix")
        )
    }

    String getFolderPrefix(IOSBuilderInfo bi) {
        "${releaseConf.projectDirName}/${variantsConf.mainVariant.fullVersionString}/${bi.target}/${bi.configuration}"
    }
}
