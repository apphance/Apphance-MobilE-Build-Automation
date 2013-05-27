package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.ios.builder.IOSArtifactProvider
import com.apphance.ameba.plugins.ios.parsers.MobileProvisionParser
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static java.net.URLEncoder.encode
import static java.util.ResourceBundle.getBundle
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
    MobileProvisionParser mpParser
    @Inject
    IOSArtifactProvider artifactProvider

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    @TaskAction
    void prepareAvailableArtifactsInfo() {
        def udids = [:]
        variantsConf.variants.each { v ->
            l.lifecycle("Preparing artifact for ${v.name}")
            prepareArtifacts(v)
            udids.put(v.target, mpParser.udids(v.mobileprovision.value))
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

    private void prepareArtifacts(AbstractIOSVariant variant) {
        def bi = artifactProvider.builderInfo(variant)

        def zipDist = artifactProvider.zipDistribution(bi)
        if (zipDist.location.exists())
            releaseConf.distributionZipFiles.put(bi.id, zipDist)

        def dSym = artifactProvider.dSYMZip(bi)
        if (dSym.location.exists())
            releaseConf.dSYMZipFiles.put(bi.id, dSym)

        def ahSym = artifactProvider.ahSYM(bi)
        if (ahSym.location.exists())
            releaseConf.ahSYMDirs.put(bi.id, ahSym)

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

    private void fileIndexArtifact(String otaFolderPrefix) {
        def aa = new AmebaArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.fileIndexFile = aa
    }

    private void plainFileIndexArtifact(String otaFolderPrefix) {
        def aa = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.plainFileIndexFile = aa
    }

    private void otaIndexFileArtifact(String otaFolderPrefix) {
        def aa = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.otaIndexFile = aa
    }

    private void qrCodeArtifact() {
        def aa = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"),
                location: new File(releaseConf.targetDirectory, "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"))
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        releaseConf.QRCodeFile = aa
    }

    private void prepareQRCode() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), "utf-8")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), releaseConf.QRCodeFile.location)
        l.lifecycle("QRCode created: ${aa.location}")
    }

    private void prepareOtaIndexFile() {
        def urlMap = [:]
        variantsConf.variants.each { v ->
            if (releaseConf.manifestFiles[v.name]) {
                l.lifecycle("Preparing OTA configuration for ${v.name}")
                def encodedUrl = encode(releaseConf.manifestFiles[v.name].url.toString(), "utf-8")
                urlMap.put(v.name, "itms-services://?action=download-manifest&url=${encodedUrl}")
            } else {
                l.warn("Skipping preparing OTA configuration for ${v.name} -> missing manifest")
            }
        }
        l.lifecycle("OTA urls: $urlMap")
        def rb = bundle('index')
        def binding = [
                baseUrl: releaseConf.otaIndexFile.url,
                title: conf.projectName.value,
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
        def tmpl = loadTemplate('index.html')
        def result = fillTemplate(tmpl, binding)
        templateToFile(releaseConf.otaIndexFile.location, result)
        l.lifecycle("Ota index created: ${releaseConf.otaIndexFile}")
        ant.copy(file: releaseConf.iconFile, tofile: new File(releaseConf.otaIndexFile.location.parentFile, releaseConf.iconFile.name))
    }

    private void prepareFileIndexFile(def udids) {
        def tmpl = loadTemplate('file_index.html')
        def rb = bundle('file_index')
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                title: conf.projectName.value,
                targets: conf.targets,
                configurations: conf.configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                conf: conf,
                releaseConf: releaseConf,
                udids: udids,
                rb: rb
        ]
        def result = fillTemplate(tmpl, binding)
        templateToFile(releaseConf.fileIndexFile.location, result)
        l.lifecycle("File index created: ${releaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        def tmpl = loadTemplate('plain_file_index.html')
        def rb = bundle('plain_file_index')
        def binding = [
                baseUrl: releaseConf.plainFileIndexFile.url,
                title: conf.projectName.value,
                targets: conf.targets,
                configurations: conf.configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                conf: conf,
                releaseConf: releaseConf,
                rb: rb
        ]
        def result = fillTemplate(tmpl, binding)
        templateToFile(releaseConf.plainFileIndexFile.location, result)
        l.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile}")
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
