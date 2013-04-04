package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.buildplugin.AndroidBuildListener
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.google.inject.Inject
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Build listener that provides .apk file in ota dir.
 *
 */
class AndroidReleaseApkListener implements AndroidBuildListener {

    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    AndroidProjectConfiguration androidConf
    AndroidReleaseConfiguration androidReleaseConf
    AntBuilder ant
    CommandExecutor executor

    static Logger logger = Logging.getLogger(AndroidReleaseApkListener.class)

    String getFolderPrefix() {
        return "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
    }

    @Inject
    AndroidReleaseApkListener(Project project, CommandExecutor executor, AndroidReleaseConfiguration androidReleaseConf) {
        use(PropertyCategory) {
            this.conf = project.getProjectConfiguration()
            this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
            this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
            this.ant = project.ant
            this.executor = executor
            this.androidReleaseConf = androidReleaseConf
        }
    }

    public void buildDone(Project project, AndroidBuilderInfo bi) {
        AmebaArtifact apkArtifact = prepareApkArtifact(bi)
        project.ant {
            copy(file: bi.originalFile, tofile: apkArtifact.location)
        }
    }

    private AmebaArtifact prepareApkArtifact(AndroidBuilderInfo bi) {
        AmebaArtifact artifact = new AmebaArtifact()
        artifact.name = "APK ${bi.debugRelease} file for ${bi.variant}"
        artifact.url = new URL(releaseConf.baseUrl, "${getFolderPrefix()}/${bi.filePrefix}.apk")
        artifact.location = new File(releaseConf.otaDirectory, "${getFolderPrefix()}/${bi.filePrefix}.apk")
        return artifact
    }

    void buildArtifactsOnly(Project project, String variant, String debugRelease = null) {
        if (variant != null && debugRelease == null) {
            debugRelease = androidConf.debugRelease[variant]
        }
        if (conf.versionString != null) {
            AndroidSingleVariantApkBuilder builder = new AndroidSingleVariantApkBuilder(project, androidConf)
            AndroidBuilderInfo bi = builder.buildApkArtifactBuilderInfo(variant, debugRelease)
            logger.lifecycle("Adding variant APK artifact ${bi.id}")
            androidReleaseConf.apkFiles.put(bi.id, prepareApkArtifact(bi))
        } else {
            logger.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }
}
