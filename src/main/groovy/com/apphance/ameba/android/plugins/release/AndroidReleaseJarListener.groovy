package com.apphance.ameba.android.plugins.release

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidBuilderInfo
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.android.plugins.buildplugin.AndroidBuildListener
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Listener that builds .jar file for library.
 *
 */
class AndroidReleaseJarListener implements AndroidBuildListener {

    ProjectHelper projectHelper
    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    AndroidProjectConfiguration androidConf
    AndroidReleaseConfiguration androidReleaseConf
    AntBuilder ant

    static Logger logger = Logging.getLogger(AndroidReleaseApkListener.class)

    AndroidReleaseJarListener(Project project, AntBuilder ant) {
        use(PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
            this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
            this.androidReleaseConf = AndroidReleaseConfigurationRetriever.getAndroidReleaseConfiguration(project)
            this.ant = ant
        }
    }

    String getFolderPrefix() {
        return "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
    }

    public void buildDone(Project project, AndroidBuilderInfo bi) {
        AmebaArtifact apkArtifact = prepareJarArtifact(bi)
        project.ant {
            copy(file: bi.originalFile, tofile: apkArtifact.location)
        }
    }

    private AmebaArtifact prepareJarArtifact(AndroidBuilderInfo bi) {
        AmebaArtifact artifact = new AmebaArtifact()
        artifact.name = "Jar ${bi.debugRelease} file for ${bi.variant}"
        artifact.url = new URL(releaseConf.baseUrl, "${getFolderPrefix()}/${bi.filePrefix}.jar")
        artifact.location = new File(releaseConf.otaDirectory, "${getFolderPrefix()}/${bi.filePrefix}.jar")
        return artifact
    }

    void buildArtifactsOnly(Project project, String variant, String debugRelease = null) {
        if (variant != null && debugRelease == null) {
            debugRelease = androidConf.debugRelease[variant]
        }
        if (conf.versionString != null) {
            AndroidSingleVariantJarBuilder builder = new AndroidSingleVariantJarBuilder(project, androidConf)
            AndroidBuilderInfo bi = builder.buildJarArtifactBuilderInfo(variant, debugRelease)
            logger.lifecycle("Adding variant JAR artifact ${bi.id}")
            androidReleaseConf.jarFiles.put(bi.id, prepareJarArtifact(bi))
        } else {
            logger.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

}
