package com.apphance.ameba.plugins.android

import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.Project

import static com.apphance.ameba.executor.AntExecutor.CLEAN

/**
 * Builds Jar for the project - one per variant.
 *
 */
class AndroidSingleVariantJarBuilder extends AbstractAndroidSingleVariantBuilder {

    AndroidSingleVariantJarBuilder(Project project, AntExecutor executor) {
        super(project, executor)
    }

    @Override
    void buildSingle(AndroidBuilderInfo bi) {
        antExecutor.executeTarget bi.tmpDir, CLEAN
        if (bi.variant != null) {
            project.ant {
                copy(todir: new File(bi.tmpDir, 'res/raw'), failonerror: false, overwrite: 'true', verbose: 'true') {
                    fileset(dir: new File(variantsDir, bi.variant),
                            includes: '*', excludes: 'market_variant.txt')
                }
            }
        }
        antExecutor.executeTarget bi.tmpDir, bi.mode.name().toLowerCase()
        logger.lifecycle("Jar file created: ${bi.originalFile}")
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }
}