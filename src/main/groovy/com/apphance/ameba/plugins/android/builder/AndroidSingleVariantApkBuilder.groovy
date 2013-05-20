package com.apphance.ameba.plugins.android.builder

import static com.apphance.ameba.executor.AntExecutor.CLEAN

/**
 * Builds APK from the project - one per variant.
 *
 */
@com.google.inject.Singleton
class AndroidSingleVariantApkBuilder extends AbstractAndroidSingleVariantBuilder {

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        antExecutor.executeTarget bi.tmpDir, CLEAN
        if (bi.variantDir?.exists()) {
            ant.copy(todir: bi.tmpDir, failonerror: false, overwrite: true, verbose: true) {
                fileset(dir: bi.variantDir, includes: '*', excludes: 'market_variant.txt')
            }
        } else {
            logger.lifecycle("No files copied because directory ${bi.variantDir} does not exists")
        }
        antExecutor.executeTarget bi.tmpDir, bi.mode.lowerCase()
        logger.lifecycle("File created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(bi)
        }
    }
}