package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.release.AndroidBuildListener
import com.apphance.ameba.plugins.android.release.AndroidReleaseApkListener
import com.apphance.ameba.plugins.android.release.AndroidReleaseJarListener
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject
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
    private AndroidConfiguration androidConf
    @Inject
    private AndroidReleaseConfiguration androidReleaseConf
    @Inject
    private AndroidVariantsConfiguration variantsConf

    @TaskAction
    public void availableArtifactsInfo() {

        AndroidBuildListener listener
        if (androidConf.isLibrary()) {
            listener = new AndroidReleaseJarListener(project, androidConf, androidReleaseConf)
        } else {
            listener = new AndroidReleaseApkListener(project, androidConf, androidReleaseConf)
        }
        variantsConf.variants.each {
            listener.buildArtifactsOnly(project, it)
        }
        if (androidConf.versionString.value) {
            String otaFolderPrefix = "${androidReleaseConf.projectDirName}/${androidConf.versionString.value}"
            prepareFileIndexArtifact(otaFolderPrefix)
            preparePlainFileIndexArtifact(otaFolderPrefix)
            prepareOtaIndexFile()
            prepareFileIndexFile(variantsConf.variants*.name)
            preparePlainFileIndexFile()
        } else {
            l.debug('Skipping building artifacts, the build is not versioned')
        }
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name: "The file index file: ${androidConf.projectName.value}",
                url: new URL(androidReleaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(androidReleaseConf.otaDir, "${otaFolderPrefix}/file_index.html")
        )
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        androidReleaseConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name: "The plain file index file: ${androidConf.projectName.value}",
                url: new URL(androidReleaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(androidReleaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        androidReleaseConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareOtaIndexFile() {
        String otaFolderPrefix = "${androidReleaseConf.projectDirName}/${androidConf.versionString.value}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${androidConf.projectName.value}",
                url: new URL(androidReleaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(androidReleaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource('index.html')
        def rb = getBundle("${this.class.package.name}.index", androidReleaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: otaIndexFile.url,
                title: androidConf.projectName.value,
                version: androidConf.versionString.value,
                releaseNotes: androidReleaseConf.releaseNotes,
                currentDate: androidReleaseConf.buildDate,
                iconFileName: androidReleaseConf.projectIconFile.value.name,
                androidVariantsConf: variantsConf,
                androidReleaseConf: androidReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), 'utf-8')
        androidReleaseConf.otaIndexFile = otaIndexFile
        l.lifecycle("Ota index created: ${otaIndexFile}")
        project.ant.copy(file: androidReleaseConf.projectIconFile.value.name, tofile: new File(otaIndexFile.location.parentFile,
                androidReleaseConf.projectIconFile.value.name))
        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), 'utf-8')
        File outputFile = new File(androidReleaseConf.targetDirectory, "qrcode-${androidConf.projectName.value}-${androidConf.versionString.value}.png")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(androidReleaseConf.versionedApplicationUrl, "qrcode-${androidConf.projectName.value}-${androidConf.versionString.value}.png"),
                location: outputFile)
        androidReleaseConf.QRCodeFile = qrCodeArtifact
        l.lifecycle("QRCode created: ${qrCodeArtifact.location}")
    }

    private void prepareFileIndexFile(Collection<String> variants) {
        URL fileIndexTemplate = getClass().getResource('file_index.html')
        def rb = getBundle("${getClass().package.name}.file_index", androidReleaseConf.locale, getClass().classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: androidReleaseConf.fileIndexFile.url,
                title: androidConf.projectName.value,
                variants: variants,
                apkFiles: androidReleaseConf.apkFiles,
                version: androidConf.versionString.value,
                currentDate: androidReleaseConf.buildDate,
                androidVariantsConf: variantsConf,
                androidReleaseConf: androidReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(fileIndexTemplate).make(binding)
        androidReleaseConf.fileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("File index created: ${androidReleaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        URL plainFileIndexTemplate = this.class.getResource('plain_file_index.html')
        ResourceBundle rb = getBundle("${this.class.package.name}.plain_file_index",
                androidReleaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: androidReleaseConf.plainFileIndexFile.url,
                title: androidConf.projectName.value,
                apkFiles: androidReleaseConf.apkFiles,
                version: androidConf.versionString.value,
                currentDate: androidReleaseConf.buildDate,
                androidVariantsConf: variantsConf,
                androidReleaseConf: androidReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        androidReleaseConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Plain file index created: ${androidReleaseConf.plainFileIndexFile}")
    }
}
