package com.apphance.flow.plugins.android.builder

import com.apphance.flow.configuration.android.AndroidArchiveType
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.plugins.release.AmebaArtifact

import javax.inject.Inject

import static com.apphance.flow.configuration.android.AndroidArchiveType.APK
import static com.apphance.flow.configuration.android.AndroidArchiveType.JAR

class AndroidArtifactProvider {

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf

    AndroidBuilderInfo builderInfo(AndroidVariantConfiguration avc) {
        String mode = avc.mode.lowerCase()
        String variablePart = "$mode-${avc.name}"

        AndroidBuilderInfo bi = new AndroidBuilderInfo(
                variant: avc.name,
                mode: avc.mode,
                tmpDir: avc.tmpDir,
                buildDir: binDir(avc),
                variantDir: avc.variantDir?.value,
                fullReleaseName: "${conf.projectName.value}-${variablePart}-${conf.fullVersionString}",
                filePrefix: "${conf.projectName.value}-${variablePart}-${conf.fullVersionString}"
        )
        bi.originalFile = new File(binDir(avc), conf.isLibrary() ? 'classes.jar': "${conf.projectName.value}-${avc.mode.lowerCase()}.apk")
        bi
    }

    private File binDir(AndroidVariantConfiguration avc) {
        new File(new File(conf.tmpDir, avc.name), 'bin')
    }

    AmebaArtifact artifact(AndroidBuilderInfo abi) {
        AndroidArchiveType type = conf.isLibrary() ? JAR : APK
        def name = "${getFolderPrefix()}/${abi.filePrefix}.${type.lowerCase()}"
        new AmebaArtifact(
                name: "${type.name()} ${abi.mode} file for ${abi.variant}",
                url: new URL(releaseConf.baseURL, name),
                location: new File(releaseConf.otaDir, name)
        )
    }

    private String getFolderPrefix() {
        "${releaseConf.projectDirName}/${conf.fullVersionString}"
    }
}
