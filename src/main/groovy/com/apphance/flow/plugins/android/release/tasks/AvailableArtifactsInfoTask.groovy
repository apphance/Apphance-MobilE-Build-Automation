package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.flow.util.file.FileDownloader.downloadFile
import static com.apphance.flow.util.file.FileManager.getHumanReadableSize
import static java.net.URLEncoder.encode
import static java.util.ResourceBundle.getBundle

class AvailableArtifactsInfoTask extends DefaultTask {

    static final NAME = 'prepareAvailableArtifactsInfo'
    String description = 'Prepares information about available artifacts for mail message to include'
    String group = FLOW_RELEASE

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AndroidVariantsConfiguration variantsConf
    @Inject AndroidArtifactProvider artifactBuilder

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    @TaskAction
    void availableArtifactsInfo() {

        conf.isLibrary() ? buildJarArtifacts() : buildAPKArtifacts()

        String otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"

        prepareMailMsgArtifact()
        prepareFileIndexArtifact(otaFolderPrefix)
        preparePlainFileIndexArtifact(otaFolderPrefix)
        prepareOTAIndexFileArtifact(otaFolderPrefix)
        prepareQRCodeArtifact()

        prepareMailMsg()
        prepareIconFile()
        prepareFileIndexFile()
        preparePlainFileIndexFile()
        prepareOTAIndexFile()
        prepareQRCode()
    }

    @PackageScope
    void buildJarArtifacts() {
        variantsConf.variants.each {
            def bi = artifactBuilder.builderInfo(it)
            releaseConf.jarFiles.put(bi.id, artifactBuilder.artifact(bi))
        }
    }

    @PackageScope
    void buildAPKArtifacts() {
        variantsConf.variants.each {
            def bi = artifactBuilder.builderInfo(it)
            releaseConf.apkFiles.put(bi.id, artifactBuilder.artifact(bi))
        }
    }

    @PackageScope
    void prepareMailMsgArtifact() {
        releaseConf.mailMessageFile = new FlowArtifact(
                name: 'Mail message file',
                url: new URL(releaseConf.versionedApplicationUrl, 'message_file.html'),
                location: new File(releaseConf.targetDir, 'message_file.html'))
        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
    }

    @PackageScope
    void prepareFileIndexArtifact(String otaFolderPrefix) {
        releaseConf.fileIndexFile = new FlowArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html")
        )
        releaseConf.fileIndexFile.location.parentFile.mkdirs()
        releaseConf.fileIndexFile.location.delete()
    }

    @PackageScope
    void preparePlainFileIndexArtifact(String otaFolderPrefix) {
        releaseConf.plainFileIndexFile = new FlowArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        releaseConf.plainFileIndexFile.location.parentFile.mkdirs()
        releaseConf.plainFileIndexFile.location.delete()
    }

    @PackageScope
    void prepareOTAIndexFileArtifact(String otaFolderPrefix) {
        releaseConf.otaIndexFile = new FlowArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        releaseConf.otaIndexFile.location.parentFile.mkdirs()
        releaseConf.otaIndexFile.location.delete()
    }

    @PackageScope
    void prepareQRCodeArtifact() {
        def qrCodeFileName = "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"
        def qrCodeFile = new File(releaseConf.targetDir, qrCodeFileName)
        releaseConf.QRCodeFile = new FlowArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, qrCodeFileName),
                location: qrCodeFile)
        releaseConf.QRCodeFile.location.parentFile.mkdirs()
        releaseConf.QRCodeFile.location.delete()

    }

    @PackageScope
    void prepareMailMsg() {
        def rb = bundle('mail_message')
        releaseConf.releaseMailSubject = fillMailSubject(rb)

        def binding = [
                title: conf.projectName.value,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                fileSize: fileSize(),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]

        def result = fillTemplate(loadTemplate('mail_message.html'), binding)
        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')

        logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile.location}")
    }

    private String fillMailSubject(ResourceBundle rb) {
        String subject = rb.getString('Subject')
        Eval.me('conf', conf, /"$subject"/)
    }

    private String fileSize() {
        getHumanReadableSize((releaseConf as AndroidReleaseConfiguration).apkFiles[variantsConf.mainVariant].location.size())
    }

    private void prepareIconFile() {
        ant.copy(
                file: new File(project.rootDir, releaseConf.iconFile.value.path),
                tofile: new File(releaseConf.otaIndexFile.location.parentFile, releaseConf.iconFile.value.name)
        )
    }

    @PackageScope
    void prepareFileIndexFile() {
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                title: conf.projectName.value,
                variants: variantsConf.variants*.name,
                apkFiles: releaseConf.apkFiles,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
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
                apkFiles: releaseConf.apkFiles,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('plain_file_index')
        ]
        def result = fillTemplate(loadTemplate('plain_file_index.html'), binding)
        templateToFile(releaseConf.plainFileIndexFile.location, result)
        logger.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile.location}")
    }

    @PackageScope
    void prepareOTAIndexFile() {
        def binding = [
                baseUrl: releaseConf.otaIndexFile.url,
                title: conf.projectName.value,
                version: conf.versionString,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.value.name,
                variantsConf: variantsConf,
                releaseConf: releaseConf,
                rb: bundle('index')
        ]
        def result = fillTemplate(loadTemplate('index.html'), binding)
        templateToFile(releaseConf.otaIndexFile.location, result)
        logger.lifecycle("OTA index created: ${releaseConf.otaIndexFile.location}")
    }

    @PackageScope
    void prepareQRCode() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), 'utf-8')
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
