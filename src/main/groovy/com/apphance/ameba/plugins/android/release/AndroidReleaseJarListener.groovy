package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import static org.gradle.api.logging.Logging.getLogger

/**
 * Listener that builds .jar file for library.
 *
 */
class AndroidReleaseJarListener implements AndroidBuildListener {

    private Logger l = getLogger(getClass())

    private AndroidConfiguration conf
    private AndroidReleaseConfiguration releaseConf
    private AntBuilder ant

    AndroidReleaseJarListener(Project project, AndroidConfiguration conf, AndroidReleaseConfiguration releaseConf) {
        this.conf = conf
        this.releaseConf = releaseConf
        this.ant = project.ant
    }

    @Override
    void buildDone(Project project, AndroidBuilderInfo bi) {
        AmebaArtifact apkArtifact = prepareJarArtifact(bi)
        project.ant {
            copy(file: bi.originalFile, tofile: apkArtifact.location)
        }
    }

    @Override
    void buildArtifactsOnly(Project project, AndroidVariantConfiguration avc) {
        if (conf.versionString.value?.trim()) {
            def builder = new AndroidSingleVariantJarBuilder(project, conf)
            def bi = builder.buildJarArtifactBuilderInfo(avc)
            l.lifecycle("Adding variant JAR artifact ${bi.id}")
            releaseConf.jarFiles.put(bi.id, prepareJarArtifact(bi))
        } else {
            l.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

    private AmebaArtifact prepareJarArtifact(AndroidBuilderInfo bi) {
        AmebaArtifact artifact = new AmebaArtifact()

        artifact.name = "Jar ${bi.debugRelease} file for ${bi.variant}"
        artifact.url = new URL(releaseConf.projectURL.value, "${getFolderPrefix()}/${bi.filePrefix}.jar")
        artifact.location = new File(releaseConf.otaDirectory, "${getFolderPrefix()}/${bi.filePrefix}.jar")

        artifact
    }

    private String getFolderPrefix() {
        "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
    }

}
