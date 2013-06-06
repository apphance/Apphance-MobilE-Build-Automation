package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.executor.AndroidExecutor
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
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject ApphanceConfiguration apphanceConf
    @Inject AntBuilder ant
    @Inject AndroidArtifactProvider artifactProvider
    @Inject AntExecutor antExecutor
    @Inject AndroidExecutor androidExecutor

    AndroidVariantConfiguration variant

    @TaskAction
    void singleVariant() {

        androidExecutor.updateProject(variant.tmpDir, conf.target.value, conf.projectName.value)

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
        if (builderInfo.originalFile.exists()) {
            log.lifecycle("File created: ${builderInfo.originalFile}")

            if (releaseConf.enabled || apphanceConf.enabled) {
                log.lifecycle("Copying file ${builderInfo.originalFile.absolutePath} to ${artifactProvider.artifact(builderInfo).location.absolutePath}")
                ant.copy(file: builderInfo.originalFile, tofile: artifactProvider.artifact(builderInfo).location)
            }
        } else {
            log.lifecycle("File ${builderInfo.originalFile} was not created. Probably due to bad signing configuration in ant.properties")
        }
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}