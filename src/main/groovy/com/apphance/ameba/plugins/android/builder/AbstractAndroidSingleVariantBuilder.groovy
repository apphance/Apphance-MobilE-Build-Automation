package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.google.inject.Singleton
import org.gradle.api.logging.Logger

import javax.inject.Inject

import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static org.gradle.api.logging.Logging.getLogger

/**
 * Base builder. Builds binary files - APK or JAR.
 */
@Singleton
class AndroidSingleVariantBuilder {

    Logger log = getLogger(getClass())

    @Inject org.gradle.api.AntBuilder ant
    @Inject AntExecutor antExecutor
    @Inject AndroidVariantsConfiguration variantsConf
    @Inject AndroidArtifactProvider artifactProvider

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
        log.lifecycle("File created: ${bi.originalFile}")

        log.lifecycle("Copying file ${bi.originalFile.absolutePath} to ${artifactProvider.artifact(bi).location.absolutePath}")
        ant.copy(file: bi.originalFile, tofile: artifactProvider.artifact(bi).location)
    }
}
