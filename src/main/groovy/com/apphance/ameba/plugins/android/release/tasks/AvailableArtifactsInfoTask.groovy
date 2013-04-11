package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.release.AndroidBuildListener
import com.apphance.ameba.plugins.android.release.AndroidReleaseApkListener
import com.apphance.ameba.plugins.android.release.AndroidReleaseJarListener
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.google.inject.Inject
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class AvailableArtifactsInfoTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'prepareAvailableArtifactsInfo'
    String description = 'Prepares information about available artifacts for mail message to include'
    String group = AMEBA_RELEASE

    @Inject AndroidConfiguration androidConfiguration
    @Inject AndroidReleaseConfiguration androidReleaseConf
    @Inject AndroidVariantsConfiguration variantsConf

    @Inject CommandExecutor executor

    private ProjectReleaseConfiguration releaseConf
    private AndroidProjectConfiguration androidConf

    @TaskAction
    public void availableArtifactsInfo() {
        this.releaseConf = getProjectReleaseConfiguration(project)
        this.androidConf = getAndroidProjectConfiguration(project)

        AndroidBuildListener listener
        if (androidConfiguration.isLibrary()) {
            listener = new AndroidReleaseJarListener(project, executor, androidReleaseConf)
        } else {
            listener = new AndroidReleaseApkListener(project, executor, androidReleaseConf)
        }
        variantsConf.variants*.name.each {
            listener.buildArtifactsOnly(project, it, null)
        }
        if (androidConfiguration.versionString.value) {
            String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${androidConfiguration.versionString.value}"
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
                name: "The file index file: ${androidConfiguration.projectName.value}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/file_index.html")
        )
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        androidReleaseConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name: "The plain file index file: ${androidConfiguration.projectName.value}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        androidReleaseConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareOtaIndexFile() {
        String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${androidConfiguration.versionString.value}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${androidConfiguration.projectName.value}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource('index.html')
        ResourceBundle rb = getBundle("${this.class.package.name}.index", releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: otaIndexFile.url,
                title: androidConfiguration.projectName.value,
                androidConf: androidConf,
                version: androidConfiguration.versionString.value,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.name,
                androidReleaseConf: androidReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), 'utf-8')
        androidReleaseConf.otaIndexFile = otaIndexFile
        l.lifecycle("Ota index created: ${otaIndexFile}")
        project.ant.copy(file: releaseConf.iconFile, tofile: new File(otaIndexFile.location.parentFile,
                releaseConf.iconFile.name))
        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), 'utf-8')
        File outputFile = new File(releaseConf.targetDirectory, "qrcode-${androidConfiguration.projectName.value}-${androidConfiguration.versionString.value}.png")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${androidConfiguration.projectName.value}-${androidConfiguration.versionString.value}.png"),
                location: outputFile)
        releaseConf.qrCodeFile = qrCodeArtifact
        l.lifecycle("QRCode created: ${qrCodeArtifact.location}")
    }

    private void prepareFileIndexFile(Collection<String> variants) {
        URL fileIndexTemplate = this.class.getResource('file_index.html')
        ResourceBundle rb = getBundle("${this.class.package.name}.file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: androidReleaseConf.fileIndexFile.url,
                title: androidConfiguration.projectName.value,
                variants: variants,
                apkFiles: androidReleaseConf.apkFiles,
                version: androidConfiguration.versionString.value,
                currentDate: releaseConf.buildDate,
                androidConf: androidConf,
                releaseConf: releaseConf,
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
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: androidReleaseConf.plainFileIndexFile.url,
                title: androidConfiguration.projectName.value,
                apkFiles: androidReleaseConf.apkFiles,
                version: androidConfiguration.versionString.value,
                currentDate: releaseConf.buildDate,
                androidConf: androidConf,
                releaseConf: releaseConf,
                androidReleaseConf: androidReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        androidReleaseConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Plain file index created: ${androidReleaseConf.plainFileIndexFile}")
    }
}
