package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser
import com.apphance.ameba.plugins.ios.MPParser
import com.apphance.ameba.plugins.ios.release.IOSReleaseConfigurationOLD
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static com.apphance.ameba.plugins.ios.release.IOSReleaseConfigurationRetriever.getIosReleaseConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static org.gradle.api.logging.Logging.getLogger

class PrepareAvailableArtifactsInfoTask {

    private l = getLogger(getClass())

    private Project project
    private CommandExecutor executor
    private IOSExecutor iosExecutor
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf
    private IOSProjectConfiguration iosConf
    private IOSReleaseConfigurationOLD iosReleaseConf

    PrepareAvailableArtifactsInfoTask(Project project, CommandExecutor executor, IOSExecutor iosExecutor) {
        this.project = project
        this.executor = executor
        this.iosExecutor = iosExecutor
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
        this.iosConf = getIosProjectConfiguration(project)
        this.iosReleaseConf = getIosReleaseConfiguration(project)
    }

    void prepareAvailableArtifactsInfo() {
        def udids = [:]
        def iosReleaseListener = new IOSReleaseListener(project, executor, iosExecutor)
        iosConf.allBuildableVariants.each { v ->
            l.lifecycle("Preparing artifact for ${v.id}")
            iosReleaseListener.buildArtifactsOnly(project, v.target, v.configuration)
            File mobileProvisionFile = IOSXCodeOutputParser.findMobileProvisionFile(project, v.target, iosConf.configurations[0], true)
            if (conf.versionString != null) {
                udids.put(v.target, MPParser.readUdids(mobileProvisionFile.toURI().toURL()))
            } else {
                l.lifecycle("Skipping retrieving udids -> the build is not versioned")
            }
        }
        if (conf.versionString != null) {
            String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
            prepareFileIndexArtifact(otaFolderPrefix)
            preparePlainFileIndexArtifact(otaFolderPrefix)
            prepareOtaIndexFile(iosConf.targets, iosConf.configurations, project.ant)
            prepareFileIndexFile(udids)
            preparePlainFileIndexFile()
        } else {
            l.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name: "The file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/file_index.html"))
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        iosReleaseConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name: "The plain file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/plain_file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        iosReleaseConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareOtaIndexFile(Collection<String> targets, Collection<String> configurations, org.gradle.api.AntBuilder ant) {
        String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        def urlMap = [:]
        l.lifecycle("Skipping preparing OTA configuration for excluded builds: ${iosConf.excludedBuilds}")
        iosConf.allBuildableVariants.each { v ->
            if (iosReleaseConf.manifestFiles[v.id]) {
                l.lifecycle("Preparing OTA configuration for ${v.id}")
                def encodedUrl = URLEncoder.encode(iosReleaseConf.manifestFiles[v.id].url.toString(), "utf-8")
                urlMap.put(v.id, "itms-services://?action=download-manifest&url=${encodedUrl}")
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
                targets: targets,
                configurations: configurations,
                version: conf.fullVersionString,
                releaseNotes: releaseConf.releaseNotes,
                currentDate: releaseConf.buildDate,
                iconFileName: releaseConf.iconFile.name,
                urlMap: urlMap,
                iosConf: iosConf,
                rb: rb
        ]
        URL otaIndexTemplate = this.class.getResource("index.html")
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), "utf-8")
        iosReleaseConf.otaIndexFile = otaIndexFile
        l.lifecycle("Ota index created: ${otaIndexFile}")
        ant.copy(file: releaseConf.iconFile, tofile: new File(otaIndexFile.location.parentFile, releaseConf.iconFile.name))
        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), "utf-8")
        File outputFile = new File(releaseConf.targetDirectory, "qrcode-${conf.projectName}-${conf.fullVersionString}.png")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: "QR Code",
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName}-${conf.fullVersionString}.png"),
                location: outputFile)
        releaseConf.qrCodeFile = qrCodeArtifact
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
                baseUrl: iosReleaseConf.fileIndexFile.url,
                title: conf.projectName,
                targets: iosConf.targets,
                configurations: iosConf.configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                iosConf: iosConf,
                releaseConf: releaseConf,
                iosReleaseConf: iosReleaseConf,
                udids: udids,
                rb: rb
        ]
        def result = engine.createTemplate(fileIndexTemplate).make(binding)
        iosReleaseConf.fileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("File index created: ${iosReleaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        URL plainFileIndexTemplate = this.class.getResource("plain_file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".plain_file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = l.debugEnabled
        def binding = [
                baseUrl: iosReleaseConf.plainFileIndexFile.url,
                title: conf.projectName,
                targets: iosConf.targets,
                configurations: iosConf.configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                iosConf: iosConf,
                releaseConf: releaseConf,
                iosReleaseConf: iosReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        iosReleaseConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Plain file index created: ${iosReleaseConf.plainFileIndexFile}")
    }

}
