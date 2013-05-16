package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.configuration.android.AndroidArchiveType
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

import static com.apphance.ameba.configuration.android.AndroidArchiveType.APK
import static com.apphance.ameba.configuration.android.AndroidArchiveType.JAR

class AndroidArtifactProvider {

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidReleaseConfiguration releaseConf

    AndroidBuilderInfo jarArtifactBuilderInfo(AndroidVariantConfiguration avc) {
        def bi = builderInfo(avc)
        bi.originalFile = new File(binDir(avc), 'classes.jar')
        bi
    }

    AmebaArtifact jarArtifact(AndroidBuilderInfo bi) {
        artifact(bi, JAR)
    }

    AndroidBuilderInfo apkArtifactBuilderInfo(AndroidVariantConfiguration avc) {
        def bi = builderInfo(avc)
        bi.originalFile = new File(binDir(avc), "${conf.projectName.value}-${avc.mode.lowerCase()}.${APK.lowerCase()}")
        bi
    }

    private AndroidBuilderInfo builderInfo(AndroidVariantConfiguration avc) {
        String mode = avc.mode.lowerCase()
        String variablePart = "$mode-${avc.name}"

        AndroidBuilderInfo bi = new AndroidBuilderInfo(
                variant: avc.name,
                mode: avc.mode,
                tmpDir: avc.tmpDir,
                buildDir: binDir(avc),
                fullReleaseName: "${conf.projectName.value}-${variablePart}-${conf.fullVersionString}",
                filePrefix: "${conf.projectName.value}-${variablePart}-${conf.fullVersionString}"
        )
        bi
    }

    private File binDir(AndroidVariantConfiguration avc) {
        new File(new File(conf.tmpDir, avc.name), 'bin')
    }

    AmebaArtifact apkArtifact(AndroidBuilderInfo bi) {
        artifact(bi, APK)
    }

    private AmebaArtifact artifact(AndroidBuilderInfo abi, AndroidArchiveType type) {
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
