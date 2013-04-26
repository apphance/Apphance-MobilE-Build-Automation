package com.apphance.ameba.plugins.android

import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.Project

import static com.apphance.ameba.executor.AntExecutor.CLEAN

/**
 * Builds APK from the project - one per variant.
 *
 */
class AndroidSingleVariantApkBuilder extends AbstractAndroidSingleVariantBuilder {

    AndroidSingleVariantApkBuilder(Project project, AntExecutor antExecutor) {
        super(project, antExecutor)
    }

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        antExecutor.executeTarget bi.tmpDir, CLEAN
        def variantPropertiesDir = new File(variantsDir, bi.variant)
        if (bi.variant != null && variantPropertiesDir.exists()) {
            project.ant {
                copy(todir: new File(bi.tmpDir, 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                    fileset(dir: variantPropertiesDir,
                            includes: '*', excludes: 'market_variant.txt')
                }
            }
        }
        antExecutor.executeTarget bi.tmpDir, bi.mode.name().toLowerCase()
        logger.lifecycle("Apk file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }
}