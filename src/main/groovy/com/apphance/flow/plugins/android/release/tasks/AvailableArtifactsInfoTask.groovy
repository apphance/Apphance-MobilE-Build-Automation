package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.flow.util.file.FileDownloader.downloadFile
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

        prepareFileIndexArtifact(otaFolderPrefix)
        preparePlainFileIndexArtifact(otaFolderPrefix)
        prepareOTAIndexFileArtifact(otaFolderPrefix)
        prepareQRCodeArtifact()

        prepareIconFile()
        prepareFileIndexFile()
        preparePlainFileIndexFile()
        prepareOTAIndexFile()
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
    void prepareFileIndexArtifact(String otaFolderPrefix) {
        def artifact = new AmebaArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html")
        )
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
        releaseConf.fileIndexFile = artifact
    }

    @PackageScope
    void preparePlainFileIndexArtifact(String otaFolderPrefix) {
        def artifact = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
        releaseConf.plainFileIndexFile = artifact
    }

    @PackageScope
    void prepareOTAIndexFileArtifact(String otaFolderPrefix) {
        def artifact = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
        releaseConf.otaIndexFile = artifact
    }

    @PackageScope
    void prepareQRCodeArtifact() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), 'utf-8')
        def qrCodeFileName = "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"
        def qrCodeFile = new File(releaseConf.targetDir, qrCodeFileName)
        qrCodeFile.parentFile.mkdirs()
        qrCodeFile.delete()

        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), qrCodeFile)

        def artifact = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, qrCodeFileName),
                location: qrCodeFile)
        releaseConf.QRCodeFile = artifact
        logger.lifecycle("QRCode created: ${artifact.location}")
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
