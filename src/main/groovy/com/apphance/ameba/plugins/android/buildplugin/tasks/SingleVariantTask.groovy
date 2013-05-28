package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.android.builder.AndroidArtifactProvider
import com.apphance.ameba.plugins.android.builder.AndroidBuilderInfo
import org.gradle.api.AntBuilder as AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.api.logging.Logging.getLogger

class SingleVariantTask extends DefaultTask {

    Logger log = getLogger(getClass())

    String group = AMEBA_BUILD

    @Inject AndroidConfiguration conf
    @Inject AntBuilder ant
    @Inject AntExecutor antExecutor
    @Inject AndroidArtifactProvider artifactProvider
    @Inject AndroidReleaseConfiguration androidReleaseConf
    @Inject ApphanceConfiguration apphanceConf

    AndroidVariantConfiguration variant

    @TaskAction
    void singleVariant() {
        AndroidBuilderInfo builderInfo = artifactProvider.builderInfo(variant)

        log.lifecycle("Building variant ${builderInfo.variant}")
        antExecutor.executeTarget builderInfo.tmpDir, CLEAN

        if (builderInfo.variantDir?.exists()) {
            log.lifecycle("Overriding files in ${builderInfo.tmpDir} with variant files from ${builderInfo.variantDir}")
            ant.copy(todir: builderInfo.tmpDir, failonerror: true, overwrite: true, verbose: true) {
                fileset(dir: builderInfo.variantDir, includes: '**/*')
            }
        } else {
            log.lifecycle("No files copied because directory ${builderInfo.variantDir} does not exists")
        }

        antExecutor.executeTarget builderInfo.tmpDir, builderInfo.mode.lowerCase()
        log.lifecycle("File created: ${builderInfo.originalFile}")

        if (androidReleaseConf.enabled || apphanceConf.enabled) {
            log.lifecycle("Copying file ${builderInfo.originalFile.absolutePath} to ${artifactProvider.artifact(builderInfo).location.absolutePath}")
            ant.copy(file: builderInfo.originalFile, tofile: artifactProvider.artifact(builderInfo).location)
        }
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}