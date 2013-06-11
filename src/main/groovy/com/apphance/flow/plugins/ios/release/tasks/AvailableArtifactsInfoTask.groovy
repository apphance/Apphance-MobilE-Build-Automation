package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSConfiguration.FAMILIES
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.flow.util.file.FileDownloader.downloadFile
import static com.apphance.flow.util.file.FileManager.getHumanReadableSize
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

        mailMsgArtifact()
        fileIndexArtifact(otaFolderPrefix)
        plainFileIndexArtifact(otaFolderPrefix)
        otaIndexFileArtifact(otaFolderPrefix)
        qrCodeArtifact()

        prepareMailMsg()
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

    @PackageScope
    void mailMsgArtifact() {
        releaseConf.mailMessageFile = new FlowArtifact(
                name: 'Mail message file',
                url: new URL(releaseConf.versionedApplicationUrl, 'message_file.html'),
                location: new File(releaseConf.targetDir, 'message_file.html'))
        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
    }

    @PackageScope
    void fileIndexArtifact(String otaFolderPrefix) {
        releaseConf.fileIndexFile = new FlowArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html"))
        releaseConf.fileIndexFile.location.parentFile.mkdirs()
        releaseConf.fileIndexFile.location.delete()
    }

    @PackageScope
    void plainFileIndexArtifact(String otaFolderPrefix) {
        releaseConf.plainFileIndexFile = new FlowArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        releaseConf.plainFileIndexFile.location.parentFile.mkdirs()
        releaseConf.plainFileIndexFile.location.delete()
    }

    @PackageScope
    void otaIndexFileArtifact(String otaFolderPrefix) {
        releaseConf.otaIndexFile = new FlowArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        releaseConf.otaIndexFile.location.parentFile.mkdirs()
        releaseConf.otaIndexFile.location.delete()
    }

    @PackageScope
    void qrCodeArtifact() {
        releaseConf.QRCodeFile = new FlowArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"),
                location: new File(releaseConf.targetDir, "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"))
        releaseConf.QRCodeFile.location.parentFile.mkdirs()
        releaseConf.QRCodeFile.location.delete()
    }

    @PackageScope
    void prepareMailMsg() {
        def fileSize = 0
        def existingBuild = ((IOSReleaseConfiguration) releaseConf).distributionZipFiles.find {
            it.value.location != null
        }
        if (existingBuild) {
            logger.lifecycle("Main build used for size calculation: ${existingBuild.key}")
            fileSize = existingBuild.value.location.size()
        }
        def rb = bundle('mail_message')
        releaseConf.releaseMailSubject = fillMailSubject(rb)

        def dmgImgFiles = ((IOSReleaseConfiguration) releaseConf).dmgImageFiles

        def binding = [
                title: conf.projectName.value,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                installable: dmgImgFiles,
                mainTarget: conf.iosVariantsConf.mainVariant.target,
                families: FAMILIES,
                fileSize: getHumanReadableSize(fileSize),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]
        if (dmgImgFiles.size() > 0) {
            FAMILIES.each { family ->
                if (dmgImgFiles["${family}-${conf.iosVariantsConf.mainVariant.target}"] == null) {
                    throw new GradleException("Wrongly configured family or target: ${family}-${conf.iosVariantsConf.mainVariant.target} missing")
                }
            }
        }
        def result = fillTemplate(loadTemplate('mail_message.html'), binding)
        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')

        logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile.location}")
    }

    private String fillMailSubject(ResourceBundle rb) {
        String subject = rb.getString('Subject')
        Eval.me("conf", conf, /"$subject"/)
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
    void prepareQRCode() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), "utf-8")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), releaseConf.QRCodeFile.location)
        logger.lifecycle("QRCode created: ${releaseConf.QRCodeFile.location}")
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
