package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.Project

import static com.apphance.ameba.executor.AntExecutor.CLEAN


/**
 * Builds Jar for the project - one per variant.
 *
 */
class AndroidSingleVariantJarBuilder extends AbstractAndroidSingleVariantBuilder {

    AndroidSingleVariantJarBuilder(Project project, AndroidConfiguration androidConf) {
        super(project, androidConf)
    }

    AndroidBuilderInfo buildJarArtifactBuilderInfo(AndroidVariantConfiguration avc) {
        String debugReleaseLowercase = avc.mode.value?.toLowerCase()
        String variablePart = debugReleaseLowercase + "-${avc.name}"
        File binDir = new File(new File(androidConf.tmpDir.value, avc.name), "bin")
        AndroidBuilderInfo bi = new AndroidBuilderInfo(
                variant: avc.name,
                debugRelease: avc.mode.value?.toLowerCase(),
                tmpDir: avc.tmpDir,
                buildDirectory: binDir,
                originalFile: new File(binDir, "classes.jar"),
                fullReleaseName: "${androidConf.projectName.value}-${variablePart}-${androidConf.fullVersionString}",
                filePrefix: "${androidConf.projectName.value}-${variablePart}-${androidConf.fullVersionString}")
        bi
    }

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        def antExecutor = new AntExecutor(bi.tmpDir)
        antExecutor.executeTarget CLEAN
        if (bi.variant != null) {
            project.ant {
                copy(todir: new File(bi.tmpDir, 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                    fileset(dir: new File(variantsDir, bi.variant),
                            includes: '*', excludes: 'market_variant.txt')
                }
            }
        }
        antExecutor.executeTarget bi.debugRelease.toLowerCase()
        logger.lifecycle("Jar file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }
}