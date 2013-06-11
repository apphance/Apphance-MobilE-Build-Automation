package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.plugins.release.tasks.AbstractAvailableArtifactsInfoTask
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSConfiguration.FAMILIES
import static com.apphance.flow.util.file.FileManager.getHumanReadableSize
import static java.net.URLEncoder.encode

class AvailableArtifactsInfoTask extends AbstractAvailableArtifactsInfoTask {

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject MobileProvisionParser mpParser
    @Inject IOSArtifactProvider artifactProvider

    @PackageScope
    void prepareOtherArtifacts() {
        def udids = [:]
        variantsConf.variants.each { v ->
            logger.lifecycle("Preparing artifact for ${v.name}")
            prepareArtifacts(v)
            udids.put(v.name, mpParser.udids(v.mobileprovision.value))
        }

        prepareFileIndexFile(udids)
    }

    @PackageScope
    void prepareArtifacts(AbstractIOSVariant variant) {
        def bi = artifactProvider.builderInfo(variant)

        def zipDist = artifactProvider.zipDistribution(bi)
        if (zipDist.location.exists())
            releaseConf.distributionZipFiles.put(bi.id, zipDist)

        def dSym = artifactProvider.dSYMZip(bi)
        if (dSym.location.exists())
            releaseConf.dSYMZipFiles.put(bi.id, dSym)

        def ahSym = artifactProvider.ahSYM(bi)
        if (ahSym.location.exists()) {
            releaseConf.ahSYMDirs.put(bi.id, ahSym)
            ahSym.location.listFiles().each {
                ahSym.childArtifacts << new FlowArtifact(location: it, name: it.name, url: "${ahSym.url.toString()}/${it.name}".toURL())
            }
        }

        def ipa = artifactProvider.ipa(bi)
        if (ipa.location.exists())
            releaseConf.ipaFiles.put(bi.id, ipa)

        def manifest = artifactProvider.manifest(bi)
        if (manifest.location.exists())
            releaseConf.manifestFiles.put(bi.id, manifest)

        def mobileprovision = artifactProvider.mobileprovision(bi)
        if (mobileprovision.location.exists())
            releaseConf.mobileProvisionFiles.put(bi.id, mobileprovision)
    }

    @Override
    @PackageScope
    Map mailMsgBinding() {
        def fileSize = 0
        def existingBuild = ((IOSReleaseConfiguration) releaseConf).distributionZipFiles.find {
            it.value.location != null
        }
        if (existingBuild) {
            logger.lifecycle("Main build used for size calculation: ${existingBuild.key}")
            fileSize = existingBuild.value.location.size()
        }
        def dmgImgFiles = ((IOSReleaseConfiguration) releaseConf).dmgImageFiles

        basicBinding + [
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                installable: dmgImgFiles,
                mainTarget: conf.iosVariantsConf.mainVariant.target,
                families: FAMILIES,
                fileSize: getHumanReadableSize(fileSize),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: bundle('mail_message')
        ]
    }

    @PackageScope
    void prepareFileIndexFile(def udids) {
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                conf: conf,
                releaseConf: releaseConf,
                variantsConf: variantsConf,
                udids: udids,
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
                conf: conf,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('plain_file_index')
        ]
    }

    @Override
    @PackageScope
    Map otaIndexFileBinding() {
        def urlMap = [:]
        variantsConf.variants.each { v ->
            if (releaseConf.manifestFiles[v.name]) {
                logger.info("Preparing OTA configuration for ${v.name}")
                def encodedUrl = encode(releaseConf.manifestFiles[v.name].url.toString(), "utf-8")
                urlMap.put(v.name, "itms-services://?action=download-manifest&url=${encodedUrl}")
            } else {
                logger.warn("Skipping preparing OTA configuration for ${v.name} -> missing manifest")
            }
        }
        logger.info("OTA urls: $urlMap")
        basicBinding + [
                baseUrl: releaseConf.otaIndexFile.url,
                releaseNotes: releaseConf.releaseNotes,
                iconFileName: releaseConf.iconFile.value.name,
                urlMap: urlMap,
                conf: conf,
                variantsConf: variantsConf,
                rb: bundle('index')
        ]
    }
}
