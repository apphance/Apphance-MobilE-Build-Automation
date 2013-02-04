package com.apphance.ameba.android

import org.gradle.api.Project

/**
 * Builds Jar for the project - one per variant.
 *
 */
class AndroidSingleVariantJarBuilder extends AbstractAndroidSingleVariantBuilder {

    AndroidSingleVariantJarBuilder(Project project, AndroidProjectConfiguration androidProjectConfiguration) {
        super(project, androidProjectConfiguration)
    }

    AndroidBuilderInfo buildJarArtifactBuilderInfo(String variant, String debugRelease) {
        if (variant != null && debugRelease == null) {
            debugRelease = androidConf.debugRelease[variant]
        }
        String debugReleaseLowercase = debugRelease?.toLowerCase()
        String variablePart = debugReleaseLowercase + "-${variant}"
        File binDir = new File(androidConf.tmpDirs[variant], "bin")
        AndroidBuilderInfo bi = new AndroidBuilderInfo(
                variant: variant,
                debugRelease: debugRelease,
                buildDirectory: binDir,
                originalFile: new File(binDir, "classes.jar"),
                fullReleaseName: "${conf.projectName}-${variablePart}-${conf.fullVersionString}",
                filePrefix: "${conf.projectName}-${variablePart}-${conf.fullVersionString}")
        bi
    }

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        projectHelper.executeCommand(project, androidConf.tmpDirs[bi.variant], ['ant', 'clean'])
        if (bi.variant != null) {
            project.ant {
                copy(todir: new File(androidConf.tmpDirs[bi.variant], 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                    fileset(dir: new File(variantsDir, bi.variant),
                            includes: '*', excludes: 'market_variant.txt')
                }
            }
        }
        projectHelper.executeCommand(project, androidConf.tmpDirs[bi.variant], [
                'ant',
                bi.debugRelease.toLowerCase()
        ])
        logger.lifecycle("Jar file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }
}