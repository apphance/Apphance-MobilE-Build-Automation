package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
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
    AndroidConfiguration getConf() {
        super.@releaseConf as AndroidConfiguration
    }


    @PackageScope
    AndroidReleaseConfiguration getReleaseConf() {
        super.@releaseConf as AndroidReleaseConfiguration
    }

    @PackageScope
    void prepareOtherArtifacts() {

        variantsConf.variants.each {
            def bi = artifactBuilder.builderInfo(it)
            if (it.isLibrary()) {
                releaseConf.jarFiles.put(bi.id, artifactBuilder.artifact(bi))
            } else {
                releaseConf.apkFiles.put(bi.id, artifactBuilder.artifact(bi))
            }
        }

        prepareFileIndexFile()
    }

    @Override
    @PackageScope
    Map mailMsgBinding() {
        basicBinding + [
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                fileSize: fileSize(),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: bundle('mail_message')
        ]
    }

    private String fileSize() {
        getHumanReadableSize((releaseConf as AndroidReleaseConfiguration).apkFiles[variantsConf.mainVariant].location.size())
    }

    @PackageScope
    void prepareFileIndexFile() {
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                variants: variantsConf.variants*.name,
                apkFiles: releaseConf.apkFiles,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('file_index')
        ] + basicBinding
        def result = fillTemplate(loadTemplate('file_index.html'), binding)
        templateToFile(releaseConf.fileIndexFile.location, result)
        logger.lifecycle("File index created: ${releaseConf.fileIndexFile.location}")
    }

    @Override
    @PackageScope
    Map plainFileIndexFileBinding() {
        basicBinding + [
                baseUrl: releaseConf.plainFileIndexFile.url,
                apkFiles: releaseConf.apkFiles,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('plain_file_index')
        ]
    }

    @Override
    @PackageScope
    Map otaIndexFileBinding() {
        basicBinding + [
                baseUrl: releaseConf.otaIndexFile.url,
                releaseNotes: releaseConf.releaseNotes,
                iconFileName: releaseConf.releaseIcon.value.name,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('index')
        ]
    }
}
