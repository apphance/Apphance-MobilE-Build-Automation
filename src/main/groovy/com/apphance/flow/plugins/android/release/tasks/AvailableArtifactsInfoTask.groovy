package com.apphance.flow.plugins.android.release.tasks

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.plugins.release.tasks.AbstractAvailableArtifactsInfoTask
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.util.NBSModelUtil.*
import static com.apphance.flow.util.file.FileManager.getHumanReadableSize

class AvailableArtifactsInfoTask extends AbstractAvailableArtifactsInfoTask {

    @Inject AndroidVariantsConfiguration variantsConf
    @Inject AndroidArtifactProvider artifactBuilder

    @PackageScope
    AndroidReleaseConfiguration getReleaseConf() {
        super.@releaseConf as AndroidReleaseConfiguration
    }

    List<AndroidVariantConfiguration> generatedVariants = []

    Map<String, FlowArtifact> artifacts = [:]

    List<AndroidVariantConfiguration> getAndroidVariants() {
        variantsConf?.variants ?: generatedVariants
    }

    @Lazy
    AppExtension androidNBS = { getAndroidNBS(project) }()

    @PackageScope
    void prepareOtherArtifacts() {

        artifactBuilder = artifactBuilder ?: new AndroidArtifactProvider(
                fullVersionString: projectFullVersion,
                projectNameNoWhiteSpace: projectNameNoWhiteSpace,
                releaseUrlVersioned: releaseUrlVersioned,
                releaseDir: releaseDir
        )

        if (androidNBS) {
            logger.lifecycle "Detected android gradle New Build System. Configuring variants taken from android configuration."

            androidNBS.applicationVariants.all { ApplicationVariant variant ->
                logger.lifecycle "Configuring NBS variant: $variant.name, output file: $variant.outputFile "

                def flowVariant = new AndroidVariantConfiguration(variant.name)
                flowVariant.projectTmpDir = { project.file(TMP_DIR) }
                flowVariant.projectNameNoWhiteSpace = projectNameNoWhiteSpace
                flowVariant.outputFile = { variant.outputFile }
                generatedVariants << flowVariant
            }
        }

        androidVariants.each {
            def builderInfo = artifactBuilder.builderInfo(it)
            def artifact = artifactBuilder.artifact(builderInfo)
            artifacts.put(builderInfo.id, artifact)
            if (builderInfo?.originalFile?.exists()) {
                ant.copy(file: builderInfo?.originalFile, tofile: artifact.location)
            } else {
                logger.error "File does not exist: ${builderInfo?.originalFile?.absolutePath} "
            }
        }

        prepareFileIndexFile()
    }

    @Override
    @PackageScope
    Map mailMsgBinding() {
        basicBinding + [
                otaUrl: otaIndexFile?.url,
                fileIndexUrl: fileIndexFile?.url,
                releaseNotes: releaseNotes,
                fileSize: fileSize(),
                releaseMailFlags: releaseMailFlags,
                rb: bundle('mail_message')
        ]
    }

    private String fileSize() {
        getHumanReadableSize(artifacts[androidVariants[0].name].location.size())
    }

    FlowArtifact imageMontageArtifact() {
        (project.tasks.findByPath(ImageMontageTask.NAME) as ImageMontageTask)?.imageMontageArtifact
    }

    @PackageScope
    void prepareFileIndexFile() {
        def binding = [
                baseUrl: fileIndexFile.url,
                variants: androidVariants,
                mailMessageFile: mailMessageFile,
                imageMontageFile: imageMontageArtifact(),
                QRCodeFile: QRCodeFile,
                plainFileIndexFile: plainFileIndexFile,
                artifacts: artifacts,
                rb: bundle('file_index')
        ] + basicBinding
        def result = fillTemplate(loadTemplate('file_index.html'), binding)
        templateToFile(fileIndexFile.location, result)
        logger.lifecycle "File index created: ${fileIndexFile.location}"
    }

    @Override
    @PackageScope
    Map plainFileIndexFileBinding() {
        basicBinding + [
                baseUrl: plainFileIndexFile.url,
                variants: androidVariants,
                artifacts: artifacts,
                mailMessageFile: mailMessageFile,
                imageMontageFile: imageMontageArtifact(),
                QRCodeFile: QRCodeFile,
                rb: bundle('plain_file_index')
        ]
    }

    @Override
    @PackageScope
    Map otaIndexFileBinding() {
        basicBinding + [
                baseUrl: otaIndexFile.url,
                releaseNotes: releaseNotes,
                iconFileName: releaseIcon.name,
                variants: androidVariants,
                mainVariant: androidVariants[0],
                artifacts: artifacts,
                rb: bundle('index')
        ]
    }

    String getVersionString() {
        super.@versionString ?: getVersionName(project)
    }

    String getVersionCode() {
        super.@versionCode ?: getVersionCode(project)
    }
}
