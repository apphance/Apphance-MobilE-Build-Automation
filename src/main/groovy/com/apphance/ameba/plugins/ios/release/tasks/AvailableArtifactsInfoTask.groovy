package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.ios.parsers.IOSXCodeOutputParser
import com.apphance.ameba.plugins.ios.parsers.MPParser
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static org.gradle.api.logging.Logging.getLogger

class AvailableArtifactsInfoTask extends DefaultTask {

    private l = getLogger(getClass())

    static final NAME = 'prepareAvailableArtifactsInfo'
    String description = 'Prepares information about available artifacts for mail message to include'
    String group = AMEBA_RELEASE

    @Inject
    IOSConfiguration conf
    @Inject
    IOSVariantsConfiguration variantsConf
    @Inject
    IOSReleaseConfiguration releaseConf
    @Inject
    IOSXCodeOutputParser parser
    @Inject
    IOSReleaseListener releaseListener

    @TaskAction
    void prepareAvailableArtifactsInfo() {
        def udids = [:]
        variantsConf.variants.each { v ->
            l.lifecycle("Preparing artifact for ${v.name}")
            releaseListener.buildArtifactsOnly(v.target, v.configuration)
            File mobileProvisionFile = v.mobileprovision.value
            if (conf.versionString != null) {
                udids.put(v.target, MPParser.readUdids(mobileProvisionFile.toURI().toURL()))
            } else {
                l.lifecycle("Skipping retrieving udids -> the build is not versioned")
            }
        }
        if (conf.versionString != null) {
            String otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"
            prepareFileIndexArtifact(otaFolderPrefix)
            preparePlainFileIndexArtifact(otaFolderPrefix)
            prepareOtaIndexFile()
            prepareFileIndexFile(udids)
            preparePlainFileIndexFile()
        } else {
            l.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name: "The file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html"))
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        releaseConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        releaseConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareOtaIndexFile() {
        String otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        def urlMap = [:]

        variantsConf.variants.each { v ->
            if (releaseConf.manifestFiles[v.name]) {
                l.lifecycle("Preparing OTA configuration for ${v.name}")
                def encodedUrl = URLEncoder.encode(releaseConf.manifestFiles[v.name].url.toString(), "utf-8")
                urlMap.put(v.name, "itms-services://?action=download-manifest&url=${encodedUrl}")
            } else {
                l.warn("Skipping preparing OTA configuration for ${v.id} -> missing manifest")
            }
        }
        l.lifecycle("OTA urls: $urlMap")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: otaIndexFile.url,
                title: conf.projectName,
                targets: conf.targets,
                configurations: conf.configurations,
                version: conf.fullVersionString,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.name,
                urlMap: urlMap,
                conf: conf,
                rb: rb
        ]
        URL otaIndexTemplate = this.class.getResource("index.html")
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), "utf-8")
        releaseConf.otaIndexFile = otaIndexFile
        l.lifecycle("Ota index created: ${otaIndexFile}")
        ant.copy(file: releaseConf.iconFile, tofile: new File(otaIndexFile.location.parentFile, releaseConf.iconFile.name))
        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), "utf-8")
        File outputFile = new File(releaseConf.targetDirectory, "qrcode-${conf.projectName}-${conf.fullVersionString}.png")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: "QR Code",
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName}-${conf.fullVersionString}.png"),
                location: outputFile)
        releaseConf.QRCodeFile = qrCodeArtifact
        l.lifecycle("QRCode created: ${qrCodeArtifact.location}")
    }

    private void prepareFileIndexFile(def udids) {
        URL fileIndexTemplate = this.class.getResource("file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                title: conf.projectName,
                targets: conf.targets,
                configurations: conf.configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                conf: conf,
                releaseConf: releaseConf,
                udids: udids,
                rb: rb
        ]
        def result = engine.createTemplate(fileIndexTemplate).make(binding)
        releaseConf.fileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("File index created: ${releaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        URL plainFileIndexTemplate = this.class.getResource("plain_file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".plain_file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: releaseConf.plainFileIndexFile.url,
                title: conf.projectName,
                targets: conf.targets,
                configurations: conf.configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                conf: conf,
                releaseConf: releaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        releaseConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile}")
    }
}
