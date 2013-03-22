package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.plugins.android.buildplugin.AndroidBuildListener
import com.apphance.ameba.executor.command.CommandExecutor
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

    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    AndroidProjectConfiguration androidConf
    AndroidReleaseConfiguration androidReleaseConf
    AntBuilder ant
    CommandExecutor executor

    static Logger logger = Logging.getLogger(AndroidReleaseJarListener.class)

    AndroidReleaseJarListener(Project project, CommandExecutor executor) {
        use(PropertyCategory) {
            this.conf = project.getProjectConfiguration()
            this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
            this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
            this.androidReleaseConf = AndroidReleaseConfigurationRetriever.getAndroidReleaseConfiguration(project)
            this.ant = project.ant
            this.executor = executor
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
