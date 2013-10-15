package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.release.tasks.AbstractAvailableArtifactsInfoTask
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.getHumanReadableSize

class AvailableArtifactsInfoTask extends AbstractAvailableArtifactsInfoTask {

    @Inject AndroidVariantsConfiguration variantsConf
    @Inject AndroidArtifactProvider artifactBuilder

    @PackageScope
    AndroidReleaseConfiguration getReleaseConf() {
        super.@releaseConf as AndroidReleaseConfiguration
    }

    @PackageScope
    void prepareOtherArtifacts() {

        variantsConf.variants.each {
            def bi = artifactBuilder.builderInfo(it)
            releaseConf.artifacts.put(bi.id, artifactBuilder.artifact(bi))
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
        getHumanReadableSize(releaseConf.artifacts[variantsConf.mainVariant.name].location.size())
    }

    @PackageScope
    void prepareFileIndexFile() {
        def binding = [
                baseUrl: fileIndexFile.url,
                variants: variantsConf.variants*.name,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('file_index')
        ] + basicBinding
        def result = fillTemplate(loadTemplate('file_index.html'), binding)
        templateToFile(fileIndexFile.location, result)
        logger.lifecycle("File index created: ${fileIndexFile.location}")
    }

    @Override
    @PackageScope
    Map plainFileIndexFileBinding() {
        basicBinding + [
                baseUrl: plainFileIndexFile.url,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
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
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('index')
        ]
    }
}
