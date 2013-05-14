package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.AndroidArtifactProvider
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static java.net.URLEncoder.encode
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

    SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    @TaskAction
    public void availableArtifactsInfo() {

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

    private void buildJarArtifacts() {
        variantsConf.variants.each {
            def bi = artifactBuilder.jarBuilderInfo(it)
            releaseConf.jarFiles.put(bi.id, artifactBuilder.jarArtifact(bi))
        }
    }

    private void buildAPKArtifacts() {
        variantsConf.variants.each {
            def bi = artifactBuilder.apkBuilderInfo(it)
            releaseConf.apkFiles.put(bi.id, artifactBuilder.apkArtifact(bi))
        }
    }

    private void prepareFileIndexArtifact(String otaFolderPrefix) {
        def artifact = new AmebaArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/file_index.html")
        )
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
        releaseConf.fileIndexFile = artifact
    }

    private void preparePlainFileIndexArtifact(String otaFolderPrefix) {
        def artifact = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/plain_file_index.html"))
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
        releaseConf.plainFileIndexFile = artifact
    }

    private prepareOTAIndexFileArtifact(String otaFolderPrefix) {
        def artifact = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL(releaseConf.baseURL, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDir, "${otaFolderPrefix}/index.html"))
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
        releaseConf.otaIndexFile = artifact
    }

    private void prepareQRCodeArtifact() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), 'utf-8')
        def qrCodeFileName = "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"
        def qrCodeFile = new File(releaseConf.targetDirectory, qrCodeFileName)

        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), qrCodeFile)

        def artifact = new AmebaArtifact(
                name: 'QR Code',
                url: new URL(releaseConf.versionedApplicationUrl, qrCodeFileName),
                location: qrCodeFile)
        releaseConf.QRCodeFile = artifact
        l.lifecycle("QRCode created: ${artifact.location}")
    }

    private void prepareIconFile() {
        def icon = releaseConf.iconFile.value
        ant.copy(file: new File(project.rootDir, icon.path), tofile: new File(releaseConf.otaIndexFile.location.parentFile, icon.name))
    }

    private void prepareFileIndexFile() {
        def tmpl = loadTemplate('file_index.html')
        def rb = bundle('file_index')
        def binding = [
                baseUrl: releaseConf.fileIndexFile.url,
                title: conf.projectName.value,
                variants: variantsConf.variants*.name,
                apkFiles: releaseConf.apkFiles,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                androidVariantsConf: variantsConf,
                androidReleaseConf: releaseConf,
                rb: rb
        ]
        def result = fillTemplate(tmpl, binding)
        templateToFile(releaseConf.fileIndexFile.location, result)
        l.lifecycle("File index created: ${releaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        def rb = bundle('plain_file_index')
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
        def tmpl = loadTemplate('plain_file_index.html')
        def result = fillTemplate(tmpl, binding)
        templateToFile(releaseConf.plainFileIndexFile.location, result)
        l.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile}")
    }

    private void prepareOTAIndexFile() {
        def otaIndexTemplate = loadTemplate('index.html')
        def rb = bundle('index')
        def binding = [
                baseUrl: releaseConf.otaIndexFile.url,
                title: conf.projectName.value,
                version: conf.versionString,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.value.name,
                variantsConf: variantsConf,
                androidReleaseConf: releaseConf,
                rb: rb
        ]
        def result = fillTemplate(otaIndexTemplate, binding)
        templateToFile(releaseConf.otaIndexFile.location, result)
        l.lifecycle("Ota index created: ${releaseConf.otaIndexFile}")
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
