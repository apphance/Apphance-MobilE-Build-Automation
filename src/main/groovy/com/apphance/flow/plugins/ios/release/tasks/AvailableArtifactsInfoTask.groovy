package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.plugins.release.tasks.AbstractAvailableArtifactsInfoTask
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static java.net.URLEncoder.encode

class AvailableArtifactsInfoTask extends AbstractAvailableArtifactsInfoTask {

    @Inject IOSVariantsConfiguration variantsConf
    @Inject MobileProvisionParser mpParser
    @Inject IOSArtifactProvider artifactProvider

    @PackageScope
    IOSReleaseConfiguration getReleaseConf() {
        super.@releaseConf as IOSReleaseConfiguration
    }

    @PackageScope
    void prepareOtherArtifacts() {
        def udids = [:]
        variantsConf.variants.each { v ->
            logger.lifecycle("Preparing artifact for ${v.name}")
            prepareArtifacts(v)
        }
        variantsConf.variants.findAll { v -> v.mode.value == DEVICE && releaseConf.ipaFiles[v.name] }.each { v ->
            udids.put(v.name, mpParser.udids(v.mobileprovision.value))
        }

        prepareFileIndexFile(udids)
    }

    @PackageScope
    void prepareArtifacts(AbstractIOSVariant variant) {
        if (variant.mode.value == DEVICE) {
            def bi = artifactProvider.deviceInfo(variant)

            def zipDist = artifactProvider.zipDistribution(bi)
            if (zipDist.location.exists())
                releaseConf.distributionZipFiles.put(bi.id, zipDist)

            def xcArchive = artifactProvider.xcArchive(bi)
            if (xcArchive.location.exists())
                releaseConf.xcArchiveZipFiles.put(bi.id, xcArchive)

            def dSym = artifactProvider.dSYMZip(bi)
            if (dSym.location.exists())
                releaseConf.dSYMZipFiles.put(bi.id, dSym)

            def ipa = artifactProvider.ipa(bi)
            if (ipa.location.exists())
                releaseConf.ipaFiles.put(bi.id, ipa)

            def manifest = artifactProvider.manifest(bi)
            if (manifest.location.exists())
                releaseConf.manifestFiles.put(bi.id, manifest)

            def mobileprovision = artifactProvider.mobileprovision(bi)
            if (mobileprovision.location.exists())
                releaseConf.mobileProvisionFiles.put(bi.id, mobileprovision)

            def ahSym = artifactProvider.ahSYM(bi)
            if (ahSym.location.exists()) {
                releaseConf.ahSYMDirs.put(bi.id, ahSym)
                ahSym.location.listFiles().each {
                    ahSym.childArtifacts << new FlowArtifact(location: it, name: it.name, url: "${ahSym.url.toString()}/${it.name}".toURL())
                }
            }
        }
        if (variant.mode.value == SIMULATOR) {
            def sBi = artifactProvider.simInfo(variant)
            IOSFamily.values().each { family ->
                def fa = artifactProvider.simulator(sBi, family)
                if (fa.location.exists())
                    releaseConf.dmgImageFiles.put("${family.iFormat()}-$sBi.id" as String, fa)
            }
        }
        if (variant.mode.value == FRAMEWORK) {
            def fBi = artifactProvider.frameworkInfo(variant)
            def framework = artifactProvider.framework(fBi)
            if (framework.location.exists()) {
                releaseConf.frameworkFiles.put(fBi.id, framework)
            }
        }
    }

    @Override
    @PackageScope
    Map mailMsgBinding() {
        basicBinding + [
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                installable: releaseConf.dmgImageFiles,
                families: IOSFamily.values(),
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
                rb: bundle('file_index'),
                families: IOSFamily.values()
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
                families: IOSFamily.values(),
                rb: bundle('plain_file_index')
        ]
    }

    @Override
    @PackageScope
    Map otaIndexFileBinding() {
        def urlMap = [:]
        variantsConf.variants.each { v ->
            switch (v.mode.value) {
                case DEVICE:
                    def manifest = releaseConf.manifestFiles[v.name]
                    if (manifest && manifest?.location?.exists()) {
                        def encodedUrl = encode(manifest.url.toString(), 'utf-8')
                        urlMap.put(v.name, "itms-services://?action=download-manifest&amp;url=${encodedUrl}")
                    }
                    break
                case SIMULATOR:
                    IOSFamily.values().each { f ->
                        def bi = artifactProvider.simInfo(v)
                        def fa = artifactProvider.simulator(bi, f)
                        if (fa.location.exists())
                            urlMap.put("${f.iFormat()}-$v.name".toString(), fa.url)
                    }
                    break
                case FRAMEWORK:
                    def bi = artifactProvider.frameworkInfo(v)
                    def fa = artifactProvider.framework(bi)
                    if (fa.location.exists())
                        urlMap.put(bi.id, fa.url)
                    break
            }
        }
        logger.info("OTA urls: $urlMap")
        basicBinding + [
                baseUrl: releaseConf.otaIndexFile.url,
                releaseNotes: releaseConf.releaseNotes,
                iconFileName: releaseConf.releaseIcon.value.name,
                urlMap: urlMap,
                conf: conf,
                variantsConf: variantsConf,
                families: IOSFamily.values(),
                rb: bundle('index')
        ]
    }
}
