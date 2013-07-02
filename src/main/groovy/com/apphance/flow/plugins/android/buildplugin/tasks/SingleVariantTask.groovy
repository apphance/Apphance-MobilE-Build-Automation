package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.android.builder.AndroidBuilderInfo
import org.gradle.api.AntBuilder as AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class SingleVariantTask extends DefaultTask {

    String group = FLOW_BUILD

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

        logger.lifecycle("Building variant ${builderInfo.variant}")
        antExecutor.executeTarget builderInfo.tmpDir, CLEAN

        if (builderInfo.variantDir?.exists()) {
            logger.lifecycle("Overriding files in ${builderInfo.tmpDir} with variant files from ${builderInfo.variantDir}")
            ant.copy(todir: builderInfo.tmpDir, failonerror: true, overwrite: true, verbose: true) {
                fileset(dir: builderInfo.variantDir, includes: '**/*')
            }
        } else {
            logger.lifecycle("No files copied because directory ${builderInfo.variantDir} does not exists")
        }

        if (variant.oldPackage.value && variant.newPackage.value) {
            def replacePackageTask = project.tasks[ReplacePackageTask.NAME] as ReplacePackageTask
            replacePackageTask.replace(variant.tmpDir, variant.oldPackage.value, variant.newPackage.value, variant.newLabel.value, variant.newName.value)
        }

        antExecutor.executeTarget builderInfo.tmpDir, builderInfo.mode.lowerCase()
        if (builderInfo.originalFile.exists()) {
            logger.lifecycle("File created: ${builderInfo.originalFile}")

            if (releaseConf.enabled) {
                logger.lifecycle("Copying file ${builderInfo.originalFile.absolutePath} to ${artifactProvider.artifact(builderInfo).location.absolutePath}")
                ant.copy(file: builderInfo.originalFile, tofile: artifactProvider.artifact(builderInfo).location)
            }
        } else {
            logger.lifecycle("File ${builderInfo.originalFile} was not created. Probably due to bad signing configuration in ant.properties")
        }
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}