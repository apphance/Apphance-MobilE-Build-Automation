package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.ios.builder.IOSArtifactProvider
import com.apphance.ameba.plugins.ios.parsers.MobileProvisionParser
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static java.net.URLEncoder.encode
import static java.util.ResourceBundle.getBundle

class AvailableArtifactsInfoTask extends DefaultTask {

    static final NAME = 'prepareAvailableArtifactsInfo'
    String description = 'Prepares information about available artifacts for mail message to include'
    String group = FLOW_RELEASE

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject MobileProvisionParser mpParser
    @Inject IOSArtifactProvider artifactProvider

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    @TaskAction
    void availableArtifactsInfo() {
        def udids = [:]
        variantsConf.variants.each { v ->
            logger.lifecycle("Preparing artifact for ${v.name}")
            prepareArtifacts(v)
            udids.put(v.name, mpParser.udids(v.mobileprovision.value))
        }
        String otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"
        fileIndexArtifact(otaFolderPrefix)
        plainFileIndexArtifact(otaFolderPrefix)
        otaIndexFileArtifact(otaFolderPrefix)
        qrCodeArtifact()

        prepareFileIndexFile(udids)
        preparePlainFileIndexFile()
        prepareOtaIndexFile()
        prepareQRCode()
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
                ahSym.childArtifacts << new AmebaArtifact(location: it, name: it.name, url: "${ahSym.url.toString()}/${it.name}".toURL())
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

    @PackageScope
    void fileIndexArtifact(String otaFolderPrefix) {
        def aa = new AmebaArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.fileIndexFile = aa
    }

    @PackageScope
    void plainFileIndexArtifact(String otaFolderPrefix) {
        def aa = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.plainFileIndexFile = aa
    }

    @PackageScope
    void otaIndexFileArtifact(String otaFolderPrefix) {
        def aa = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.otaIndexFile = aa
    }

    @PackageScope
    void qrCodeArtifact() {
        def aa = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"),
                location: new File(releaseConf.targetDir, "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.QRCodeFile = aa
    }

    @PackageScope
    void prepareQRCode() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), "utf-8")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), releaseConf.QRCodeFile.location)
        logger.lifecycle("QRCode created: ${releaseConf.QRCodeFile.location}")
    }

    @PackageScope
    void prepareOtaIndexFile() {
        def urlMap = [:]
        variantsConf.variants.each { v ->
            if (releaseConf.manifestFiles[v.name]) {
                logger.lifecycle("Preparing OTA configuration for ${v.name}")
                def encodedUrl = encode(releaseConf.manifestFiles[v.name].url.toString(), "utf-8")
                urlMap.put(v.name, "itms-services://?action=download-manifest&url=${encodedUrl}")
            } else {
                logger.warn("Skipping preparing OTA configuration for ${v.name} -> missing manifest")
            }
        }
        logger.lifecycle("OTA urls: $urlMap")
        def binding = [
                baseUrl: releaseConf.otaIndexFile.url,
                title: conf.projectName.value,
                version: conf.fullVersionString,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.value.name,
                urlMap: urlMap,
                conf: conf,
                variantsConf: variantsConf,
                rb: bundle('index')
        ]
        def result = fillTemplate(loadTemplate('index.html'), binding)
        templateToFile(releaseConf.otaIndexFile.location, result)
        logger.lifecycle("OTA index created: ${releaseConf.otaIndexFile.location}")
        ant.copy(file: releaseConf.iconFile.value, tofile: new File(releaseConf.otaIndexFile.location.parentFile, releaseConf.iconFile.value.name))
    }

    @PackageScope
    void prepareFileIndexFile(def udids) {
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                title: conf.projectName.value,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                conf: conf,
                releaseConf: releaseConf,
                variantsConf: variantsConf,
                udids: udids,
                rb: bundle('file_index')
        ]
        def result = fillTemplate(loadTemplate('file_index.html'), binding)
        templateToFile(releaseConf.fileIndexFile.location, result)
        logger.lifecycle("File index created: ${releaseConf.fileIndexFile.location}")
    }

    @PackageScope
    void preparePlainFileIndexFile() {
        def binding = [
                baseUrl: releaseConf.plainFileIndexFile.url,
                title: conf.projectName.value,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                conf: conf,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('plain_file_index')
        ]
        def result = fillTemplate(loadTemplate('plain_file_index.html'), binding)
        templateToFile(releaseConf.plainFileIndexFile.location, result)
        logger.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile.location}")
    }

    private ResourceBundle bundle(String id) {
        def c = getClass()
        getBundle("${c.package.name}.$id", releaseConf.locale, c.classLoader)
    }

    private URL loadTemplate(String template) {
        getClass().getResource(template)
    }

    private Writable fillTemplate(URL tmpl, Map binding) {
        templateEngine.createTemplate(tmpl).make(binding)
    }

    private void templateToFile(File f, Writable tmpl) {
        f.write(tmpl.toString(), 'UTF-8')
    }
}
