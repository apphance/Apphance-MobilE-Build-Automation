package com.apphance.ameba.android

import java.io.File
import java.util.Collection

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory


class AndroidSingleVariantBuilder {

    static Logger logger = Logging.getLogger(AndroidSingleVariantBuilder.class)
    Project project
    ProjectHelper projectHelper
    ProjectConfiguration conf
    AndroidProjectConfiguration androidConf
    File variantsDir

    AndroidSingleVariantBuilder(Project project, AndroidProjectConfiguration androidProjectConfiguration) {
        use (PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.androidConf = androidProjectConfiguration
            this.variantsDir = new File(project.rootDir, "variants")
        }
    }

    void updateAndroidConfigurationWithVariants() {
        androidConf.variants = []
        if (hasVariants()) {
            androidConf.variants = getVariants()
        } else {
            androidConf.variants = ["Debug", "Release"]
        }
        if (androidConf.variants.empty) {
            throw new GradleException("variants directory should contain at least one variant!")
        }
    }

    AndroidArtifactBuilderInfo buildApkArtifactBuilderInfo(Project project, String variant, String debugRelease) {
        if (variant != null && debugRelease == null) {
            debugRelease = getDebugRelease(project, variant)
        }
        String debugReleaseLowercase = debugRelease?.toLowerCase()
        String variablePart = debugReleaseLowercase + (variant == null ? "" : "-${variant}")
        File binDir = new File(project.rootDir, "srcTmp/bin")
        AndroidArtifactBuilderInfo bi = new AndroidArtifactBuilderInfo(
                variant: variant,
                debugRelease: debugRelease,
                buildDirectory : binDir,
                originalFile : new File(binDir, "${conf.projectName}-${debugReleaseLowercase}.apk"),
                fullReleaseName : "${conf.projectName}-${variablePart}-${conf.fullVersionString}",
                folderPrefix : "${conf.projectDirectoryName}/${conf.fullVersionString}",
                filePrefix : "${conf.projectName}-${variablePart}-${conf.fullVersionString}")
        return bi
    }

    AndroidArtifactBuilderInfo buildJarArtifactBuilderInfo(Project project, String variant, String debugRelease) {
        if (variant != null && debugRelease == null) {
            debugRelease = getDebugRelease(project, variant)
        }
        String debugReleaseLowercase = debugRelease?.toLowerCase()
        String variablePart = debugReleaseLowercase + (variant == null ? "" : "-${variant}")
        File binDir = new File(project.rootDir, "srcTmp/bin")
        AndroidArtifactBuilderInfo bi = new AndroidArtifactBuilderInfo(
                variant: variant,
                debugRelease: debugRelease,
                buildDirectory : binDir,
                originalFile : new File(binDir, "classes.jar"),
                fullReleaseName : "${conf.projectName}-${variablePart}-${conf.fullVersionString}",
                folderPrefix : "${conf.projectDirectoryName}/${conf.fullVersionString}",
                filePrefix : "${conf.projectName}-${variablePart}-${conf.fullVersionString}")
        return bi
    }


    protected boolean hasVariants() {
        return variantsDir.exists() && variantsDir.isDirectory()
    }

    protected Collection<String> getVariants() {
        def variants = []
        variantsDir.eachDir {
            if (!androidConf.isBuildExcluded(it.name)) {
                variants << it.name
            }
        }
        return variants
    }

    String getDebugRelease(Project project, String variant) {
        File dir = new File(variantsDir,"${variant}")
        boolean marketVariant = dir.list().any { it == 'market_variant.txt' }
        return marketVariant ? 'Release' : 'Debug'
    }

    private AmebaArtifact prepareApkArtifact(AndroidArtifactBuilderInfo bi) {
        AmebaArtifact artifact = new AmebaArtifact()
        artifact.name = "APK ${bi.debugRelease} file for ${bi.variant}"
        artifact.url = new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}.apk")
        artifact.location = new File(conf.otaDirectory, "${bi.folderPrefix}/${bi.filePrefix}.apk")
        return artifact
    }

    private AmebaArtifact prepareJarArtifact(AndroidArtifactBuilderInfo bi) {
        AmebaArtifact artifact = new AmebaArtifact()
        artifact.name = "Jar ${bi.debugRelease} file for ${bi.variant}"
        artifact.url = new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}.jar")
        artifact.location = new File(conf.otaDirectory, "${bi.folderPrefix}/${bi.filePrefix}.jar")
        return artifact
    }

    void buildSingleApk(AndroidArtifactBuilderInfo bi) {
        AmebaArtifact apkArtifact = prepareApkArtifact(bi)
        projectHelper.executeCommand(project, new File (project.rootDir, "srcTmp"), ['ant', 'clean'])
        if (bi.variant != null) {
            project.ant {
                copy(todir : 'srcTmp/res/raw', overwrite:'true', verbose:'true') {
                    fileset(dir: new File(variantsDir, bi.variant),
                            includes:'*', excludes:'market_variant.txt')
                }
            }
        }
        projectHelper.executeCommand(project, new File (project.rootDir, "srcTmp"), [
            'ant',
            bi.debugRelease.toLowerCase()
        ])
        project.ant {
            copy (file: bi.originalFile, tofile: apkArtifact.location)
        }
    }

    void buildSingleJar(AndroidArtifactBuilderInfo bi) {
        AmebaArtifact apkArtifact = prepareJarArtifact(bi)
        projectHelper.executeCommand(project, new File (project.rootDir, "srcTmp"), ['ant', 'clean'])
        if (bi.variant != null) {
            project.ant {
                copy(todir : project.file('srcTmp/res/raw'), overwrite:'true', verbose:'true') {
                    fileset(dir: new File(variantsDir, bi.variant),
                            includes:'*', excludes:'market_variant.txt')
                }
            }
        }
        projectHelper.executeCommand(project, new File (project.rootDir, "srcTmp"), [
            'ant',
            bi.debugRelease.toLowerCase()
        ])
        project.ant {
            copy (file: bi.originalFile, tofile: apkArtifact.location)
        }
    }

    void buildArtifactsOnly(Project project, String variant, boolean isLibrary, String debugRelease = null) {
        if (variant != null && debugRelease == null) {
            debugRelease = getDebugRelease(project, variant)
        }
        if (conf.versionString != null) {
            if (isLibrary) {
                AndroidArtifactBuilderInfo bi = buildJarArtifactBuilderInfo(project, variant, debugRelease)
                logger.lifecycle("Adding variant JAR artifact ${bi.id}")
                androidConf.jarFiles.put(bi.id, prepareJarArtifact(bi))
            } else {
                AndroidArtifactBuilderInfo bi = buildApkArtifactBuilderInfo(project, variant, debugRelease)
                logger.lifecycle("Adding variant APK artifact ${bi.id}")
                androidConf.apkFiles.put(bi.id, prepareApkArtifact(bi))
            }
        } else {
            logger.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }
}
