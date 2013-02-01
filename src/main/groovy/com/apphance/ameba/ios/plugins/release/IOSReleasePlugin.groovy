package com.apphance.ameba.ios.plugins.release

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.PluginHelper
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.MPParser
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import com.apphance.ameba.util.file.FileManager
import groovy.text.SimpleTemplateEngine
import org.gradle.api.AntBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.apphance.ameba.util.file.FileDownloader.downloadFile

/**
 * Plugin for releasing iOS build.
 *
 */
class IOSReleasePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(IOSReleasePlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    IOSProjectConfiguration iosConf
    IOSReleaseConfiguration iosReleaseConf
    IOSPlistProcessor iosPlistProcessor = new IOSPlistProcessor()

    @Override
    def void apply(Project project) {
        PluginHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class, ProjectReleasePlugin.class)
        this.projectHelper = new ProjectHelper();
        this.conf = PropertyCategory.getProjectConfiguration(project)
        this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
        this.iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
        this.iosReleaseConf = IOSReleaseConfigurationRetriever.getIosReleaseConfiguration(project)
        prepareUpdateVersionTask(project)
        prepareBuildDocumentationZipTask(project)
        prepareAvailableArtifactsInfoTask(project)
        prepareMailMessageTask(project)
        IOSSingleVariantBuilder.buildListeners << new IOSReleaseListener(project, project.ant)
    }

    def void prepareBuildDocumentationZipTask(Project project) {
        def task = project.task('buildDocumentationZip')
        task.description = "Builds documentation .zip file."
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            File destZip = releaseConf.documentationZip.location
            destZip.mkdirs()
            destZip.delete()
            throw new GradleException("Documentation not yet implemented!")
        }
    }

    private void prepareAvailableArtifactsInfoTask(Project project) {
        def task = project.task('prepareAvailableArtifactsInfo')
        task.description = "Prepares information about available artifacts for mail message to include"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            def targets = iosConf.targets
            def configurations = iosConf.configurations
            def udids = [:]
            def iosReleaseListener = new IOSReleaseListener(project, project.ant)
            targets.each { target ->
                configurations.each { configuration ->
                    def id = "${target}-${configuration}".toString()
                    if (!iosConf.isBuildExcluded(id)) {
                        logger.lifecycle("Preparing artifact for ${id}")
                        iosReleaseListener.buildArtifactsOnly(project, target, configuration)
                    } else {
                        logger.lifecycle("Skipping preparing artifact for ${id} -> excluded by ${iosConf.excludedBuilds}")
                    }
                }
                File mobileprovisionFile = IOSXCodeOutputParser.findMobileProvisionFile(project, target, configurations[0], true)
                if (conf.versionString != null) {
                    udids.put(target, MPParser.readUdids(mobileprovisionFile.toURI().toURL()))
                } else {
                    logger.lifecycle("Skipping retrieving udids -> the build is not versioned")
                }
            }
            if (conf.versionString != null) {
                String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
                prepareFileIndexArtifact(otaFolderPrefix)
                preparePlainFileIndexArtifact(otaFolderPrefix)
                prepareOtaIndexFile(targets, configurations, ant)
                prepareFileIndexFile(targets, configurations, udids)
                preparePlainFileIndexFile(targets, configurations)
            } else {
                logger.lifecycle("Skipping building artifacts -> the build is not versioned")
            }
        }
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.readIOSProjectVersions)
    }


    private void prepareMailMessageTask(Project project) {
        def task = project.task('prepareMailMessage')
        task.description = "Prepares mail message which summarises the release"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            releaseConf.mailMessageFile.location.parentFile.mkdirs()
            releaseConf.mailMessageFile.location.delete()
            logger.lifecycle("Targets: ${iosConf.targets}")
            logger.lifecycle("Configurations: ${iosConf.configurations}")
            URL mailTemplate = this.class.getResource("mail_message.html")
            def fileSize = 0
            def existingBuild = iosReleaseConf.distributionZipFiles.find {
                it.value.location != null
            }
            if (existingBuild) {
                logger.lifecycle("Main build used for size calculation: ${existingBuild.key}")
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
            logger.lifecycle("Runnning template with ${binding}")
            if (iosReleaseConf.dmgImageFiles.size() > 0) {
                iosConf.families.each { family ->
                    if (iosReleaseConf.dmgImageFiles["${family}-${iosConf.mainTarget}"] == null) {
                        throw new GradleException("Wrongly configured family or target: ${family}-${iosConf.mainTarget} missing")
                    }
                }
            }
            def result = engine.createTemplate(mailTemplate).make(binding)
            releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
            logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
        }
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.prepareAvailableArtifactsInfo)
        def sendMailTask = project.sendMailMessage
        sendMailTask.dependsOn(task)
        sendMailTask.description += ",installableSimulator"
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

    private void prepareFileIndexFile(Collection<String> targets, Collection<String> configurations, def udids) {
        URL fileIndexTemplate = this.class.getResource("file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                baseUrl: iosReleaseConf.fileIndexFile.url,
                title: conf.projectName,
                targets: targets,
                configurations: configurations,
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
        logger.lifecycle("File index created: ${iosReleaseConf.fileIndexFile}")
    }

    void prepareUpdateVersionTask(Project project) {
        def task = project.task('updateVersion')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task.description = """Updates version stored in plist file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""
        task << {
            use(PropertyCategory) {
                conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
                conf.versionCode = project.readPropertyOrEnvironmentVariable('version.code') as Long
                iosPlistProcessor.incrementPlistVersion(iosConf, conf)
                logger.lifecycle("New version code: ${conf.versionCode}")
                logger.lifecycle("Updated version string to ${conf.versionString}")
            }
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    private void preparePlainFileIndexFile(Collection<String> targets, Collection<String> configurations) {
        URL plainFileIndexTemplate = this.class.getResource("plain_file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".plain_file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                baseUrl: iosReleaseConf.plainFileIndexFile.url,
                title: conf.projectName,
                targets: targets,
                configurations: configurations,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                iosConf: iosConf,
                releaseConf: releaseConf,
                iosReleaseConf: iosReleaseConf,
                rb: rb
        ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        iosReleaseConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        logger.lifecycle("Plain file index created: ${iosReleaseConf.plainFileIndexFile}")
    }

    private void prepareOtaIndexFile(Collection<String> targets, Collection<String> configurations, AntBuilder ant) {
        String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource("index.html")
        def urlMap = [:]
        targets.each { target ->
            configurations.each { configuration ->
                def id = "${target}-${configuration}".toString()
                if (!iosConf.isBuildExcluded(id)) {
                    if (iosReleaseConf.manifestFiles[id] != null) {
                        logger.lifecycle("Preparing OTA configuration for ${id}")
                        def encodedUrl = URLEncoder.encode(iosReleaseConf.manifestFiles[id].url.toString(), "utf-8")
                        urlMap.put(id, "itms-services://?action=download-manifest&url=${encodedUrl}")
                    } else {
                        logger.warn("Skipping preparing OTA configuration for ${id} -> missing manifest")
                    }
                } else {
                    logger.lifecycle("Skipping preparing OTA configuration for ${id} -> excluded by ${iosConf.excludedBuilds}")
                }
            }
        }
        logger.lifecycle("OTA urls: ${urlMap}")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
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
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), "utf-8")
        iosReleaseConf.otaIndexFile = otaIndexFile
        logger.lifecycle("Ota index created: ${otaIndexFile}")
        ant.copy(file: releaseConf.iconFile, tofile: new File(otaIndexFile.location.parentFile, releaseConf.iconFile.name))
        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), "utf-8")
        File outputFile = new File(releaseConf.targetDirectory, "qrcode-${conf.projectName}-${conf.fullVersionString}.png")
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name: "QR Code",
                url: new URL(releaseConf.versionedApplicationUrl, "qrcode-${conf.projectName}-${conf.fullVersionString}.png"),
                location: outputFile)
        releaseConf.qrCodeFile = qrCodeArtifact
        logger.lifecycle("QRCode created: ${qrCodeArtifact.location}")
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
