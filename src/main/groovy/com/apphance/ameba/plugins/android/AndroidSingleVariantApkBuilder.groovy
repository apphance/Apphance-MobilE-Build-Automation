package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.Project

import static com.apphance.ameba.executor.AntExecutor.CLEAN

/**
 * Builds APK from the project - one per variant.
 *
 */
class AndroidSingleVariantApkBuilder extends AbstractAndroidSingleVariantBuilder {

    AndroidSingleVariantApkBuilder(Project project, AndroidConfiguration androidConf) {
        super(project, androidConf)
    }

    AndroidBuilderInfo buildApkArtifactBuilderInfo(AndroidVariantConfiguration avc) {
        String debugReleaseLowercase = avc.mode.value?.toLowerCase()
        String variablePart = debugReleaseLowercase + "-${avc.name}"
        File binDir = new File(new File(androidConf.tmpDir, avc.name), 'bin')
        AndroidBuilderInfo bi = new AndroidBuilderInfo
        (
                variant: avc.name,
                debugRelease: avc.mode.value,
                tmpDir: avc.tmpDir,
                buildDirectory: binDir,
                originalFile: new File(binDir, "${androidConf.projectName.value}-${debugReleaseLowercase}.apk"),
                fullReleaseName: "${androidConf.projectName.value}-${variablePart}-${androidConf.fullVersionString}",
                filePrefix: "${androidConf.projectName.value}-${variablePart}-${androidConf.fullVersionString}"
        )
        return bi
    }

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        def antExecutor = new AntExecutor(bi.tmpDir)
        antExecutor.executeTarget CLEAN
        def variantPropertiesDir = new File(variantsDir, bi.variant)
        if (bi.variant != null && variantPropertiesDir.exists()) {
            project.ant {
                copy(todir: new File(bi.tmpDir, 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                    fileset(dir: variantPropertiesDir,
                            includes: '*', excludes: 'market_variant.txt')
                }
            }
        }
        antExecutor.executeTarget bi.debugRelease.toLowerCase()
        logger.lifecycle("Apk file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }
}