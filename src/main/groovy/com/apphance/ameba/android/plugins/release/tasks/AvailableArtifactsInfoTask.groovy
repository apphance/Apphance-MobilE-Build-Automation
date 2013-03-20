package com.apphance.ameba.android.plugins.release.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidEnvironment
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.android.plugins.buildplugin.AndroidBuildListener
import com.apphance.ameba.android.plugins.release.AndroidReleaseApkListener
import com.apphance.ameba.android.plugins.release.AndroidReleaseConfiguration
import com.apphance.ameba.android.plugins.release.AndroidReleaseJarListener
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.android.plugins.release.AndroidReleaseConfigurationRetriever.getAndroidReleaseConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class AvailableArtifactsInfoTask {

    private l = getLogger(getClass())

    private Project project
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf
    private CommandExecutor executor
    private AndroidProjectConfiguration androidConf
    private AndroidReleaseConfiguration androidReleaseConf
    private AndroidEnvironment androidEnv

    AvailableArtifactsInfoTask(Project project, CommandExecutor executor) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
        this.executor = executor
        this.androidConf = getAndroidProjectConfiguration(project)
        this.androidReleaseConf = getAndroidReleaseConfiguration(project)
        this.androidEnv = new AndroidEnvironment(project)
    }

    public void availableArtifactsInfo() {
        AndroidBuildListener listener
        def builder
        if (androidEnv.isLibrary()) {
            builder = new AndroidSingleVariantJarBuilder(project, androidConf)
            listener = new AndroidReleaseJarListener(project, executor)
        } else {
            builder = new AndroidSingleVariantApkBuilder(project, androidConf)
            listener = new AndroidReleaseApkListener(project, executor)
        }
        if (androidConf.hasVariants()) {
            androidConf.variants.each { variant ->
                listener.buildArtifactsOnly(project, variant, null)
            }
        } else {
            listener.buildArtifactsOnly(project, 'Debug', 'Debug')
            listener.buildArtifactsOnly(project, 'Release', 'Release')
        }
        if (conf.versionString) {
            String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
            prepareFileIndexArtifact(otaFolderPrefix)
            preparePlainFileIndexArtifact(otaFolderPrefix)
            prepareOtaIndexFile()
            prepareFileIndexFile(androidConf.variants)
            preparePlainFileIndexFile()
        } else {
            l.debug('Skipping building artifacts, the build is not versioned')
        }
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name: "The file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/file_index.html")
        )
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        androidReleaseConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        androidReleaseConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareOtaIndexFile() {
        String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName}",
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
                title: conf.projectName,
                androidConf: androidConf,
                version: conf.fullVersionString,
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
        File outputFile = new File(releaseConf.targetDirectory, "qrcode-${conf.projectName}-${conf.fullVersionString}.png")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName}-${conf.fullVersionString}.png"),
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
                title: conf.projectName,
                variants: variants,
                apkFiles: androidReleaseConf.apkFiles,
                version: conf.fullVersionString,
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
                title: conf.projectName,
                apkFiles: androidReleaseConf.apkFiles,
                version: conf.fullVersionString,
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
