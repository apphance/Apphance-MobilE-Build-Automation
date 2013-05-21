package com.apphance.ameba.plugins.android.builder

import static com.apphance.ameba.executor.AntExecutor.CLEAN

/**
 * Builds Jar for the project - one per variant.
 *
 */
@com.google.inject.Singleton
//TODO write spec for this class
class AndroidSingleVariantJarBuilder extends AbstractAndroidSingleVariantBuilder {

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        antExecutor.executeTarget bi.tmpDir, CLEAN
        if (bi.variantDir?.exists()) {
            ant.copy(todir: bi.tmpDir, failonerror: true, overwrite: true, verbose: true) {
                fileset(dir: bi.variantDir, includes: '**/*')
            }
        } else {
            log.lifecycle("No files copied because directory ${bi.variantDir} does not exists")
        }
        antExecutor.executeTarget bi.tmpDir, bi.mode.lowerCase()
        log.lifecycle("Jar file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(bi)
        }
    }
}