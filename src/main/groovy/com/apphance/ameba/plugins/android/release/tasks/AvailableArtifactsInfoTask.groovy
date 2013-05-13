package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.AndroidArtifactProvider
import com.apphance.ameba.plugins.release.AmebaArtifact
import javax.inject.Inject
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class AvailableArtifactsInfoTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'prepareAvailableArtifactsInfo'
    String description = 'Prepares information about available artifacts for mail message to include'
    String group = AMEBA_RELEASE

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidReleaseConfiguration releaseConf
    @Inject
    AndroidVariantsConfiguration variantsConf
    @Inject
    AndroidArtifactProvider artifactBuilder

    @TaskAction
    public void availableArtifactsInfo() {

        if (conf.isLibrary()) {
            buildJarArtifacts()
        } else {
            buildAPKArtifacts()
        }

        if (conf.fullVersionString) {
            String otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"
            prepareFileIndexArtifact(otaFolderPrefix)
            preparePlainFileIndexArtifact(otaFolderPrefix)
            prepareOtaIndexFile()
            prepareFileIndexFile(variantsConf.variants*.name)
            preparePlainFileIndexFile()
        } else {
            l.debug('Skipping building artifacts, the build is not versioned')
        }
    }

    private void buildJarArtifacts() {
        variantsConf.variants.each {
            def bi = artifactBuilder.jarArtifactBuilderInfo(it)
            releaseConf.jarFiles.put(bi.id, artifactBuilder.jarArtifact(bi))
        }
    }

    private void buildAPKArtifacts() {
        variantsConf.variants.each {
            def bi = artifactBuilder.apkArtifactBuilderInfo(it)
            releaseConf.apkFiles.put(bi.id, artifactBuilder.apkArtifact(bi))
        }
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html")
        )
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        releaseConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        releaseConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareOtaIndexFile() {
        String otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource('index.html')
        def rb = getBundle("${this.class.package.name}.index", releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: otaIndexFile.url,
                title: conf.projectName.value,
                version: conf.versionString,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.value?.name,
                variantsConf: variantsConf,
                androidReleaseConf: releaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), 'utf-8')
        releaseConf.otaIndexFile = otaIndexFile
        l.lifecycle("Ota index created: ${otaIndexFile}")

        def iconFile = releaseConf.iconFile.value
        ant.copy(file: new File(project.rootDir, iconFile.path), tofile: new File(otaIndexFile.location.parentFile, iconFile.name))

        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), 'utf-8')
        def qrCodeFile = "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"
        File outputFile = new File(releaseConf.targetDirectory, qrCodeFile)
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, qrCodeFile),
                location: outputFile)
        releaseConf.QRCodeFile = qrCodeArtifact
        l.lifecycle("QRCode created: ${qrCodeArtifact.location}")
    }

    private void prepareFileIndexFile(Collection<String> variants) {
        URL fileIndexTemplate = getClass().getResource('file_index.html')
        def rb = getBundle("${getClass().package.name}.file_index", releaseConf.locale, getClass().classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                title: conf.projectName.value,
                variants: variants,
                apkFiles: releaseConf.apkFiles,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                androidVariantsConf: variantsConf,
                androidReleaseConf: releaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(fileIndexTemplate).make(binding)
        releaseConf.fileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("File index created: ${releaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        URL plainFileIndexTemplate = this.class.getResource('plain_file_index.html')
        ResourceBundle rb = getBundle("${this.class.package.name}.plain_file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: releaseConf.plainFileIndexFile.url,
                title: conf.projectName.value,
                apkFiles: releaseConf.apkFiles,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                androidVariantsConf: variantsConf,
                androidReleaseConf: releaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        releaseConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile}")
    }
}
