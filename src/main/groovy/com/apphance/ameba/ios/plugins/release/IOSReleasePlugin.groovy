package com.apphance.ameba.ios.plugins.release

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.MPParser
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.util.file.FileManager
import groovy.text.SimpleTemplateEngine
import org.gradle.api.AntBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for releasing iOS build.
 *
 */
class IOSReleasePlugin implements Plugin<Project> {

    def l = getLogger(getClass())

    @Inject
    CommandExecutor executor

    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    IOSProjectConfiguration iosConf
    IOSReleaseConfiguration iosReleaseConf

    @Override
    def void apply(Project project) {
        this.conf = PropertyCategory.getProjectConfiguration(project)
        this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
        this.iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
        this.iosReleaseConf = IOSReleaseConfigurationRetriever.getIosReleaseConfiguration(project)
        prepareUpdateVersionTask(project)
        prepareBuildDocumentationZipTask(project)
        prepareAvailableArtifactsInfoTask(project)
        prepareMailMessageTask(project)
    }

    void prepareUpdateVersionTask(Project project) {
        def task = project.task('updateVersion')
        task.group = AMEBA_RELEASE
        task.description = """Updates version stored in plist file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""
        def iosPlistProcessor = new IOSPlistProcessor()
        task << {
            use(PropertyCategory) {
                conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
                conf.versionCode = project.readPropertyOrEnvironmentVariable('version.code') as Long
                iosPlistProcessor.incrementPlistVersion(iosConf, conf)
                l.lifecycle("New version code: ${conf.versionCode}")
                l.lifecycle("Updated version string to ${conf.versionString}")
            }
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareBuildDocumentationZipTask(Project project) {
        def task = project.task('buildDocumentationZip')
        task.description = 'Builds documentation .zip file.'
        task.group = AMEBA_RELEASE
        task << {
            File destZip = releaseConf.documentationZip.location
            destZip.mkdirs()
            destZip.delete()
            throw new GradleException('Documentation not yet implemented!')
        }
    }

    private void prepareAvailableArtifactsInfoTask(Project project) {
        def task = project.task('prepareAvailableArtifactsInfo')
        task.description = 'Prepares information about available artifacts for mail message to include'
        task.group = AMEBA_RELEASE
        task << {
            def udids = [:]
            def iosReleaseListener = new IOSReleaseListener(project, executor)
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
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.readIOSProjectVersions)
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

    private void prepareOtaIndexFile(Collection<String> targets, Collection<String> configurations, AntBuilder ant) {
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

    private void prepareMailMessageTask(Project project) {
        def task = project.task('prepareMailMessage')
        task.description = 'Prepares mail message which summarises the release'
        task.group = AMEBA_RELEASE
        task << {
            releaseConf.mailMessageFile.location.parentFile.mkdirs()
            releaseConf.mailMessageFile.location.delete()
            l.lifecycle("Targets: ${iosConf.targets}")
            l.lifecycle("Configurations: ${iosConf.configurations}")
            URL mailTemplate = this.class.getResource('mail_message.html')
            def fileSize = 0
            def existingBuild = iosReleaseConf.distributionZipFiles.find {
                it.value.location != null
            }
            if (existingBuild) {
                l.lifecycle("Main build used for size calculation: ${existingBuild.key}")
                fileSize = existingBuild.value.location.size()
            }
            ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".mail_message",
                    releaseConf.locale, this.class.classLoader)
            ProjectReleaseCategory.fillMailSubject(project, rb)
            SimpleTemplateEngine engine = new SimpleTemplateEngine()
            def binding = [
                    title: conf.projectName,
                    version: conf.fullVersionString,
                    currentDate: releaseConf.buildDate,
                    otaUrl: iosReleaseConf.otaIndexFile?.url,
                    fileIndexUrl: iosReleaseConf.fileIndexFile?.url,
                    releaseNotes: releaseConf.releaseNotes,
                    installable: iosReleaseConf.dmgImageFiles,
                    mainTarget: iosConf.mainTarget,
                    families: iosConf.families,
                    fileSize: FileManager.getHumanReadableSize(fileSize),
                    releaseMailFlags: releaseConf.releaseMailFlags,
                    rb: rb
            ]
            l.lifecycle("Runnning template with $binding")
            if (iosReleaseConf.dmgImageFiles.size() > 0) {
                iosConf.families.each { family ->
                    if (iosReleaseConf.dmgImageFiles["${family}-${iosConf.mainTarget}"] == null) {
                        throw new GradleException("Wrongly configured family or target: ${family}-${iosConf.mainTarget} missing")
                    }
                }
            }
            def result = engine.createTemplate(mailTemplate).make(binding)
            releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
            l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
        }
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.prepareAvailableArtifactsInfo)
        def sendMailTask = project.sendMailMessage
        sendMailTask.dependsOn(task)
        sendMailTask.description += ',installableSimulator'
    }

    static public final String DESCRIPTION =
        """This is the plugin that provides simple release functionality.

It provides basic release tasks, so that you can upgrade version of the application
while preparing the release and it provides post-release tasks that commit it into the repository.
Most importantly however, it produces ready-to-use OTA (Over-The-Air) package (in ota directory)
that you can copy to appropriate directory on your web server and have ready-to-use,
easily installable OTA version of your application.

Note that you need to load generic 'ameba-project-release' plugin before this plugin is loaded."""
}
