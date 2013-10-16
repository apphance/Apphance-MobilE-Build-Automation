package com.apphance.flow.plugins.android.buildplugin.tasks

import com.android.manifmerger.ManifestMerger
import com.android.utils.StdLogger
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.executor.command.CommandFailedException
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import org.gradle.api.AntBuilder as AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.android.manifmerger.MergerLog.wrapSdkLog
import static com.android.utils.StdLogger.Level.VERBOSE
import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.apphance.flow.executor.command.CommandExecutor.MAX_STD_LOG_SIZE
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST

class SingleVariantTask extends DefaultTask {

    String group = FLOW_BUILD

    Logger logger = Logging.getLogger(getClass())

    @Inject AntBuilder ant
    @Inject AndroidArtifactProvider artifactProvider
    @Inject AntExecutor antExecutor
    @Inject AndroidProjectUpdater projectUpdater
    @Inject AndroidConfiguration conf

    AndroidVariantConfiguration variant

    @TaskAction
    void singleVariant() {
        logger.lifecycle("Building variant ${variant.name}")
        projectUpdater.updateRecursively variant.tmpDir, conf.target.value, conf.projectName.value

        antExecutor.executeTarget variant.tmpDir, CLEAN

        if (variant.variantDir?.value?.exists()) {
            overrideVariantFilesAndMergeManifest(variant.tmpDir, variant.variantDir?.value)
        } else {
            logger.lifecycle("No files copied because variant directory ${variant.variantDir?.value} does not exist")
        }

        if (variant.oldPackage.value && variant.newPackage.value) {
            new PackageReplacer().replace(variant.tmpDir, variant.oldPackage.value, variant.newPackage.value, variant.newLabel.value, variant.newName.value)
        }

        new LibraryDependencyHandler().handleLibraryDependencies(variant.tmpDir)

        def builderInfo = artifactProvider.builderInfo(variant)
        executeBuildTarget builderInfo.tmpDir, builderInfo.mode.lowerCase()

        if (builderInfo.originalFile.exists()) {
            logger.lifecycle("File created: ${builderInfo.originalFile}")

            def artifact = artifactProvider.artifact(builderInfo)
            logger.lifecycle("Copying file ${builderInfo.originalFile.absolutePath} to ${artifact.location.absolutePath}")
            ant.copy(file: builderInfo.originalFile, tofile: artifact.location)

        } else {
            logger.lifecycle("File ${builderInfo.originalFile} was not created. Probably due to bad signing configuration in ant.properties")
        }
    }

    void executeBuildTarget(File rootDir, String command) {
        try {
            antExecutor.executeTarget rootDir, command
        } catch (CommandFailedException exp) {
            def output = (exp.stdoutLog && exp.stdoutLog.size() < MAX_STD_LOG_SIZE) ? exp.stdoutLog?.text : ''
            if (output.contains('method onStart in class Apphance cannot be applied to given types')) {
                logger.error "Error during source compilation. Probably some non-activity class was configured as activity in AndroidManifest.xml.\n" +
                        "Make sure that all <activity> tags in your manifest points to some activity classes and not to other classes like Fragment."
            }
            throw exp
        }
    }

    void overrideVariantFilesAndMergeManifest(File tmpDir, File variantDir) {
        logger.lifecycle("Overriding files in ${tmpDir} with variant files from ${variantDir}")
        ant.copy(todir: tmpDir, failonerror: true, overwrite: true, verbose: true) {
            fileset(dir: variantDir, includes: '**/*')
        }

        def variantManifest = new File(variantDir, ANDROID_MANIFEST)
        logger.info "Variant manifest exist: ${variantManifest.exists()}, merging enabled: ${variant.mergeManifest.value}"
        if (variant.mergeManifest.value && variantManifest.exists()) {
            mergeManifest(new File(variant.tmpDir, ANDROID_MANIFEST), project.file(ANDROID_MANIFEST), variantManifest)
        }
    }

    @groovy.transform.PackageScope
    def mergeManifest(File out, File main, File... manifestsToMerge) {
        logger.lifecycle "Merging manifests. Output: $out.absolutePath, main manifest: $main.absolutePath, to be merged: ${manifestsToMerge*.absolutePath}"
        def merger = new ManifestMerger(wrapSdkLog(new StdLogger(VERBOSE)), null);
        boolean ok = merger.process(out, main, manifestsToMerge, null, null);
        if (!ok) throw new GradleException("Error during merging manifests.")
    }

    @Override
    String getDescription() {
        "Builds ${name}"
    }
}