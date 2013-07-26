package com.apphance.flow.plugins.android.buildplugin.tasks

import com.android.manifmerger.ManifestMerger
import com.android.manifmerger.MergerLog
import com.android.utils.StdLogger
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import org.gradle.api.AntBuilder as AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.android.manifmerger.MergerLog.wrapSdkLog
import static com.android.utils.StdLogger.Level.VERBOSE
import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class SingleVariantTask extends DefaultTask {

    String group = FLOW_BUILD

    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AntBuilder ant
    @Inject AndroidArtifactProvider artifactProvider
    @Inject AntExecutor antExecutor
    @Inject AndroidProjectUpdater projectUpdater

    AndroidVariantConfiguration variant
    private FlowArtifact artifact

    @TaskAction
    void singleVariant() {
        projectUpdater.runRecursivelyInAllSubProjects(variant.tmpDir)

        def builderInfo = artifactProvider.builderInfo(variant)

        logger.lifecycle("Building variant ${builderInfo.variant}")
        antExecutor.executeTarget builderInfo.tmpDir, CLEAN

        if (builderInfo.variantDir?.exists()) {
            logger.lifecycle("Overriding files in ${builderInfo.tmpDir} with variant files from ${builderInfo.variantDir}")
            ant.copy(todir: builderInfo.tmpDir, failonerror: true, overwrite: true, verbose: true) {
                fileset(dir: builderInfo.variantDir, includes: '**/*')
            }
        } else {
            logger.lifecycle("No files copied because variant directory ${builderInfo.variantDir} does not exists")
        }

        if (variant.oldPackage.value && variant.newPackage.value) {
            def replacePackageTask = project.tasks[ReplacePackageTask.NAME] as ReplacePackageTask
            replacePackageTask.replace(variant.tmpDir, variant.oldPackage.value, variant.newPackage.value, variant.newLabel.value, variant.newName.value)
        }

        try {
            antExecutor.executeTarget builderInfo.tmpDir, builderInfo.mode.lowerCase()
        } catch (Exception exp) {
            if (exp.hasProperty('output') && exp.output.contains('method onStart in class Apphance cannot be applied to given types')) {
                logger.error "Error during source compilation. Probably some non-activity class was configured as activity in AndroidManifest.xml.\n" +
                        "Make sure that all <activity> tags in your manifest points to some activity classes and not to other classes like Fragment."
            }
            throw exp
        }
        if (builderInfo.originalFile.exists()) {
            logger.lifecycle("File created: ${builderInfo.originalFile}")

            if (releaseConf.enabled) {
                artifact = artifactProvider.artifact(builderInfo)
                logger.lifecycle("Copying file ${builderInfo.originalFile.absolutePath} to ${artifact.location.absolutePath}")
                ant.copy(file: builderInfo.originalFile, tofile: artifact.location)
            }
        } else {
            logger.lifecycle("File ${builderInfo.originalFile} was not created. Probably due to bad signing configuration in ant.properties")
        }
    }

    @groovy.transform.PackageScope
    def mergeManifest(File out, File main, File... manifestsToMerge) {
        def merger = new ManifestMerger(wrapSdkLog(new StdLogger(VERBOSE)), null);
        boolean ok = merger.process(out, main, manifestsToMerge, null, null);
        if (!ok) throw new GradleException("Error during merging manifests.")
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}