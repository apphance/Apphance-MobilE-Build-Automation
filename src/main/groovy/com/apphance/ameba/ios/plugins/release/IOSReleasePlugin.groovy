package com.apphance.ameba.ios.plugins.release;

import groovy.text.SimpleTemplateEngine

import java.io.File
import java.net.URL
import java.util.Collection

import org.gradle.api.AntBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.XMLBomAwareFileReader
import com.apphance.ameba.android.AndroidEnvironment
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.MPParser
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSSingleReleaseBuilder;
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import com.sun.org.apache.xpath.internal.XPathAPI

/**
 * Plugin for preparing reports after successful build.
 *
 */
class IOSReleasePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(IOSReleasePlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSXCodeOutputParser iosConfigurationAndTargetRetriever
    IOSProjectConfiguration iosConf

    def void apply (Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class, ProjectReleasePlugin.class)
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.iosConfigurationAndTargetRetriever = new IOSXCodeOutputParser()
            this.iosConf = this.iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
            prepareUpdateVersionTask(project)
            prepareBuildDocumentationZipTask(project)
            prepareAvailableArtifactsInfoTask(project)
            prepareMailMessageTask(project)
        }
    }

    def void prepareBuildDocumentationZipTask(Project project) {
        def task = project.task('buildDocumentationZip')
        task.description = "Builds documentation .zip file."
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            File destZip = conf.documentationZip.location
            destZip.mkdirs()
            destZip.delete()
            throw new GradleException("Documentation not yet implemented!")
            // !! prepare documentation using doxygen
            // ant.zip(destfile: destZip ) { fileset(dir: documentationDir) }
        }
    }

    private void prepareAvailableArtifactsInfoTask(Project project) {
        def task = project.task('prepareAvailableArtifactsInfo')
        task.description = "Prepares information about available artifacts for mail message to include"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        AndroidEnvironment androidEnvironment = new AndroidEnvironment(project)
        task << {
            def targets = iosConf.targets
            def configurations = iosConf.configurations
            def udids = [:]
            def singleReleaseBuilder = new IOSSingleReleaseBuilder(project, project.ant)
            targets.each { target ->
                configurations.each { configuration ->
                    def id = "${target}-${configuration}".toString()
                    if (!iosConf.isBuildExcluded(id)) {
                        logger.lifecycle("Preparing artifact for ${id}")
                        singleReleaseBuilder.buildArtifactsOnly(project, target, configuration)
                    } else {
                        logger.lifecycle("Skipping preparing artifact for ${id} -> excluded by ${iosConf.excludedBuilds}")
                    }
                }
                File mobileprovisionFile = iosConfigurationAndTargetRetriever.findMobileProvisionFile(project, target, configurations[0])
                if (conf.versionString != null){
                    udids.put(target, MPParser.readUdids(mobileprovisionFile.toURI().toURL()))
                } else {
                    logger.lifecycle("Skipping retrieving udids -> the build is not versioned")
                }
            }
            if (conf.versionString != null) {
                String otaFolderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}"
                prepareFileIndexArtifact(otaFolderPrefix)
                preparePlainFileIndexArtifact(otaFolderPrefix)
                prepareOtaIndexFile(project, targets, configurations, ant)
                prepareFileIndexFile(project, targets, configurations, udids)
                preparePlainFileIndexFile(project, targets, configurations)
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
            conf.mailMessageFile.location.parentFile.mkdirs()
            conf.mailMessageFile.location.delete()
            logger.lifecycle("Targets: ${iosConf.targets}")
            logger.lifecycle("Configurations: ${iosConf.configurations}")
            URL mailTemplate = this.class.getResource("mail_message.html")
            def fileSize = 0
            def existingBuild = this.iosConf.distributionZipFiles.find {
                it.value.location != null
            }
            if (existingBuild){
                logger.lifecycle("Main build used for size calculation: ${existingBuild.key}")
                fileSize = existingBuild.value.location.size()
            }
            ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".mail_message",
                    conf.locale, this.class.classLoader)
            projectHelper.fillMailSubject(project, rb)
            SimpleTemplateEngine engine = new SimpleTemplateEngine()
            def binding = [
                        title : this.conf.projectName,
                        version :this.conf.fullVersionString,
                        currentDate: this.conf.buildDate,
                        otaUrl : this.iosConf.otaIndexFile?.url,
                        fileIndexUrl: this.iosConf.fileIndexFile?.url,
                        releaseNotes : this.conf.releaseNotes,
                        installable : this.iosConf.dmgImageFiles,
                        mainTarget: this.iosConf.mainTarget,
                        families: this.iosConf.families,
                        fileSize : projectHelper.getHumanReadableSize(fileSize),
                        releaseMailFlags : conf.releaseMailFlags,
                        rb :rb
                    ]
            logger.lifecycle("Runnning template with ${binding}")
            if (this.iosConf.dmgImageFiles.size() > 0) {
                this.iosConf.families.each { family ->
                    if (this.iosConf.dmgImageFiles ["${family}-${this.iosConf.mainTarget}"] == null) {
                        throw new GradleException("Wrongly configured family or target: ${family}-${this.iosConf.mainTarget} missing")
                    }
                }
            }
            def result = engine.createTemplate(mailTemplate).make(binding)
            conf.mailMessageFile.location.write(result.toString(), "utf-8")
            logger.lifecycle("Mail message file created: ${conf.mailMessageFile}")
        }
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.prepareAvailableArtifactsInfo)
        def sendMailTask = project.sendMailMessage
        sendMailTask.dependsOn(task)
        sendMailTask.description += ",installableSimulator"
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name : "The file index file: ${conf.projectName}",
                url : new URL(conf.baseUrl, "${otaFolderPrefix}/file_index.html"),
                location : new File(conf.otaDirectory,"${otaFolderPrefix}/file_index.html"))
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        iosConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name : "The plain file index file: ${conf.projectName}",
                url : new URL(conf.baseUrl, "${otaFolderPrefix}/plain_file_index.html"),
                location : new File(conf.otaDirectory,"${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        iosConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareFileIndexFile(Project project,
    Collection<String> targets, Collection<String> configurations, def udids) {
        URL fileIndexTemplate = this.class.getResource("file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".file_index",
                conf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                    baseUrl: iosConf.fileIndexFile.url,
                    title: conf.projectName,
                    targets: targets,
                    configurations: configurations,
                    version: conf.fullVersionString,
                    currentDate: conf.buildDate,
                    iosConf: iosConf,
                    conf : conf,
                    udids : udids,
                    rb : rb
                ]
        def result = engine.createTemplate(fileIndexTemplate).make(binding)
        iosConf.fileIndexFile.location.write(result.toString(), "utf-8")
        logger.lifecycle("File index created: ${iosConf.fileIndexFile}")
    }

    private org.w3c.dom.Element getParsedPlist(Project project) {
        logger.debug("Reading file " + iosConf.plistFile)
        return new XMLBomAwareFileReader().readXMLFileIncludingBom(iosConf.plistFile)
    }

    def void prepareUpdateVersionTask(Project project) {
        def task = project.task('updateVersion')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task.description = """Updates version stored in plist file of the project.
           Numeric version is (incremented), String version is set from version.string property"""
        task << {
            use (PropertyCategory) {
                conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
                def root = getParsedPlist(project)
                XPathAPI.selectNodeList(root,
                        '/plist/dict/key[text()="CFBundleShortVersionString"]').each{
                            it.nextSibling.nextSibling.textContent = conf.versionString
                        }
                conf.versionCode += 1
                XPathAPI.selectNodeList(root,
                        '/plist/dict/key[text()="CFBundleVersion"]').each{
                            it.nextSibling.nextSibling.textContent = conf.versionCode
                        }
                iosConf.plistFile.write(root as String)
                logger.lifecycle("New version code: ${conf.versionCode}")
                logger.lifecycle("Updated version string to ${conf.versionString}")
            }
        }
        task.dependsOn(project.readProjectConfiguration)
    }



    private void preparePlainFileIndexFile(Project project,
    Collection<String> targets, Collection<String> configurations) {
        URL plainFileIndexTemplate = this.class.getResource("plain_file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".plain_file_index",
                conf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                    baseUrl: iosConf.plainFileIndexFile.url,
                    title: conf.projectName,
                    targets: targets,
                    configurations: configurations,
                    version: conf.fullVersionString,
                    currentDate: conf.buildDate,
                    iosConf: iosConf,
                    conf : conf,
                    rb: rb
                ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        iosConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        logger.lifecycle("Plain file index created: ${iosConf.plainFileIndexFile}")
    }

    private void prepareOtaIndexFile(Project project, Collection<String> targets, Collection<String> configurations, AntBuilder ant) {
        String otaFolderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name : "The ota index file: ${conf.projectName}",
                url : new URL(conf.baseUrl, "${otaFolderPrefix}/index.html"),
                location : new File(conf.otaDirectory,"${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource("index.html")
        def urlMap = [:]
        targets.each { target ->
            configurations.each { configuration ->
                def id = "${target}-${configuration}".toString()
                if (!iosConf.isBuildExcluded(id)) {
                    logger.lifecycle("Preparing OTA configuration for ${id}")
                    def encodedUrl = URLEncoder.encode(iosConf.manifestFiles[id].url.toString(),"utf-8")
                    urlMap.put(id,"itms-services://?action=download-manifest&url=${encodedUrl}")
                } else {
                    logger.lifecycle("Skipping preparing OTA configuration for ${id} -> excluded by ${iosConf.excludedBuilds}")
                }
            }
        }
        logger.lifecycle("OTA urls: ${urlMap}")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".index",
                conf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                    baseUrl: otaIndexFile.url,
                    title: conf.projectName,
                    targets: targets,
                    configurations: configurations,
                    version : conf.fullVersionString,
                    releaseNotes: conf.releaseNotes,
                    currentDate: conf.buildDate,
                    iconFileName: conf.iconFile.name,
                    urlMap: urlMap,
                    conf: conf,
                    iosConf: iosConf,
                    rb : rb
                ]
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), "utf-8")
        iosConf.otaIndexFile = otaIndexFile
        logger.lifecycle("Ota index created: ${otaIndexFile}")
        ant.copy(file: conf.iconFile, tofile: new File(otaIndexFile.location.parentFile, conf.iconFile.name))
        String urlEncoded = URLEncoder.encode(otaIndexFile.url.toString(), "utf-8")
        File outputFile = new File(conf.targetDirectory, "qrcode-${conf.projectName}-${conf.fullVersionString}.png")
        downloadFile(project, new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), outputFile)
        AmebaArtifact qrCodeArtifact = new AmebaArtifact(
                name : "QR Code",
                url : new URL(conf.versionedApplicationUrl, "qrcode-${conf.projectName}-${conf.fullVersionString}.png"),
                location : outputFile)
        conf.qrCodeFile  = qrCodeArtifact
        logger.lifecycle("QRCode created: ${qrCodeArtifact.location}")
    }

    void downloadFile(Project project, URL url, File file) {
        logger.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
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
