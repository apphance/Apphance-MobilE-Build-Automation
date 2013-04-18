package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import static org.gradle.api.logging.Logging.getLogger

/**
 * Build listener that provides .apk file in ota dir.
 *
 */
class AndroidReleaseApkListener implements AndroidBuildListener {

    private AndroidConfiguration conf
    private AndroidReleaseConfiguration releaseConf
    private AntBuilder ant

    private Logger l = getLogger(getClass())

    AndroidReleaseApkListener(Project project, AndroidConfiguration conf, AndroidReleaseConfiguration releaseConf) {
        this.conf = conf
        this.releaseConf = releaseConf
        this.ant = project.ant
    }

    @Override
    void buildDone(Project project, AndroidBuilderInfo bi) {
        AmebaArtifact apkArtifact = prepareApkArtifact(bi)
        project.ant {
            copy(file: bi.originalFile, tofile: apkArtifact.location)
        }
    }

    @Override
    void buildArtifactsOnly(Project project, AndroidVariantConfiguration avc) {
        if (conf.versionString != null) {
            def builder = new AndroidSingleVariantApkBuilder(project, conf)
            def bi = builder.buildApkArtifactBuilderInfo(avc)
            l.lifecycle("Adding variant APK artifact ${bi.id}")
            releaseConf.apkFiles.put(bi.id, prepareApkArtifact(bi))
        } else {
            l.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

    private AmebaArtifact prepareApkArtifact(AndroidBuilderInfo bi) {
        AmebaArtifact artifact = new AmebaArtifact()

        artifact.name = "APK ${bi.debugRelease} file for ${bi.variant}"
        artifact.url = new URL(releaseConf.baseURL, "${getFolderPrefix()}/${bi.filePrefix}.apk")
        artifact.location = new File(releaseConf.otaDir, "${getFolderPrefix()}/${bi.filePrefix}.apk")

        artifact
    }

    private String getFolderPrefix() {
        "${releaseConf.projectDirName}/${conf.fullVersionString}"
    }
}
