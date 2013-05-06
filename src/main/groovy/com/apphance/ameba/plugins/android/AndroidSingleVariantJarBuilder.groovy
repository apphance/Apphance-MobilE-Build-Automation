package com.apphance.ameba.plugins.android

import static com.apphance.ameba.executor.AntExecutor.CLEAN

/**
 * Builds Jar for the project - one per variant.
 *
 */
@com.google.inject.Singleton
class AndroidSingleVariantJarBuilder extends AbstractAndroidSingleVariantBuilder {

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        antExecutor.executeTarget bi.tmpDir, CLEAN
        if (bi.variant) {
            ant.copy(todir: new File(bi.tmpDir, 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                fileset(dir: new File(variantsConfiguration.variantsDir, bi.variant),
                        includes: '*', excludes: 'market_variant.txt')
            }
        }
        antExecutor.executeTarget bi.tmpDir, bi.mode.name().toLowerCase()
        logger.lifecycle("Jar file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(bi)
        }
    }
}