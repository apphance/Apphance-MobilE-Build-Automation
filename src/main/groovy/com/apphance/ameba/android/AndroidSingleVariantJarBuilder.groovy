package com.apphance.ameba.android

import com.apphance.ameba.executor.Command
import com.apphance.ameba.executor.CommandExecutor
import org.gradle.api.Project

/**
 * Builds Jar for the project - one per variant.
 *
 */
class AndroidSingleVariantJarBuilder extends AbstractAndroidSingleVariantBuilder {

    AndroidSingleVariantJarBuilder(Project project, AndroidProjectConfiguration androidProjectConfiguration, CommandExecutor executor) {
        super(project, androidProjectConfiguration, executor)
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
        executor.executeCommand(new Command(runDir: androidConf.tmpDirs[bi.variant], cmd: ['ant', 'clean']))
        if (bi.variant != null) {
            project.ant {
                copy(todir: new File(androidConf.tmpDirs[bi.variant], 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                    fileset(dir: new File(variantsDir, bi.variant),
                            includes: '*', excludes: 'market_variant.txt')
                }
            }
        }
        executor.executeCommand(new Command(runDir: androidConf.tmpDirs[bi.variant], cmd: ['ant', bi.debugRelease.toLowerCase()]))
        logger.lifecycle("Jar file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }
}