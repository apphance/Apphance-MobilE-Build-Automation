package com.apphance.ameba.plugins.android

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
        def variantPropertiesDir = new File(variantsConfiguration.variantsDir, bi.variant)
        if (bi.variant && variantPropertiesDir.exists()) {
            ant.copy(todir: new File(bi.tmpDir, 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                fileset(dir: variantPropertiesDir,
                        includes: '*', excludes: 'market_variant.txt')
            }
        }
        antExecutor.executeTarget bi.tmpDir, bi.mode.lowerCase()
        logger.lifecycle("File created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(bi)
        }
    }
}