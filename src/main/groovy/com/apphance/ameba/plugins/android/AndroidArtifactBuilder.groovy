package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidArchiveType
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

import static com.apphance.ameba.configuration.android.AndroidArchiveType.APK
import static com.apphance.ameba.configuration.android.AndroidArchiveType.JAR

class AndroidArtifactBuilder {

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
        bi.originalFile = new File(binDir(avc), "${conf.projectName.value}-${avc.mode.name().toLowerCase()}.apk")
        bi
    }

    private AndroidBuilderInfo builderInfo(AndroidVariantConfiguration avc) {
        String mode = avc.mode.name().toLowerCase()
        String variablePart = "$mode-${avc.name}"

        AndroidBuilderInfo bi = new AndroidBuilderInfo(
                variant: avc.name,
                mode: avc.mode,
                tmpDir: avc.tmpDir,
                buildDirectory: binDir(avc),
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
        new AmebaArtifact(
                name: "${type.name()} ${abi.mode} file for ${abi.variant}",
                url: new URL(releaseConf.baseURL, "${getFolderPrefix()}/${abi.filePrefix}.${type.extension}"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix()}/${abi.filePrefix}.${type.extension}")
        )
    }

    private String getFolderPrefix() {
        "${releaseConf.projectDirName}/${conf.fullVersionString}"
    }
}
