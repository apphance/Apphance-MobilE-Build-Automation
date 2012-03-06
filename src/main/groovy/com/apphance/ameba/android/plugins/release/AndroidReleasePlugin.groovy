package com.apphance.ameba.android.plugins.release

import groovy.text.SimpleTemplateEngine

import java.io.File
import java.net.URL
import java.util.Collection

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidEnvironment
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.AndroidSingleVariantBuilder
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin;

class AndroidReleasePlugin implements Plugin<Project>{

    static Logger logger = Logging.getLogger(AndroidReleasePlugin.class)

    Project project
    ProjectHelper projectHelper
    ProjectConfiguration conf
    AndroidProjectConfigurationRetriever androidProjectConfigurationRetriever
    AndroidProjectConfiguration androidConf
    AndroidManifestHelper manifestHelper

    public void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class, ProjectReleasePlugin.class)
        use (PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.androidProjectConfigurationRetriever = new AndroidProjectConfigurationRetriever()
            this.androidConf = this.androidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
            this.manifestHelper = new AndroidManifestHelper()
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
            File javadocDir = project.file("build/docs/javadoc")
            project.ant.zip(destfile: destZip, basedir : javadocDir)
            logger.lifecycle("Zipped documentation to ${destZip}")
        }
        task.dependsOn(project.javadoc, project.readProjectConfiguration, project.prepareForRelease)
    }

    private void prepareAvailableArtifactsInfoTask(Project project) {
        def builder  = new AndroidSingleVariantBuilder(project, androidConf)
        def task = project.task('prepareAvailableArtifactsInfo')
        task.description = "Prepares information about available artifacts for mail message to include"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        AndroidEnvironment androidEnvironment = new AndroidEnvironment(project)
        task << {
            if (builder.hasVariants()) {
                androidConf.variants.each { variant ->
                    builder.buildArtifactsOnly(project, variant, androidEnvironment.isLibrary())
                }
            } else {
                builder.buildArtifactsOnly(project, null, androidEnvironment.isLibrary(), 'Debug')
                builder.buildArtifactsOnly(project, null, androidEnvironment.isLibrary(), 'Release')
            }
            if (conf.versionString != null) {
                String otaFolderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}"
                prepareFileIndexArtifact(otaFolderPrefix)
                preparePlainFileIndexArtifact(otaFolderPrefix)
                prepareOtaIndexFile(project, androidConf.variants)
                prepareFileIndexFile(project, androidConf.variants)
                preparePlainFileIndexFile(project, androidConf.variants)
            } else {
                logger.lifecycle("Skipping building artifacts -> the build is not versioned")
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
    }

    private void prepareMailMessageTask(Project project) {
        def task = project.task('prepareMailMessage')
        task.description = "Prepares mail message which summarises the release"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            conf.mailMessageFile.location.parentFile.mkdirs()
            conf.mailMessageFile.location.delete()
            logger.lifecycle("Variants: ${androidConf.variants}")
            URL mailTemplate = this.class.getResource("mail_message.html")
            def mainBuild = "${androidConf.mainVariant}"
            logger.lifecycle("Main build used for size calculation: ${mainBuild}")
            def fileSize = androidConf.apkFiles[mainBuild].location.size()
            ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".mail_message",
                    conf.locale, this.class.classLoader)
            projectHelper.fillMailSubject(project, rb)
            SimpleTemplateEngine engine = new SimpleTemplateEngine()
            def binding = [
                        title : this.conf.projectName,
                        version :this.conf.fullVersionString,
                        currentDate: this.conf.buildDate,
                        otaUrl : this.androidConf.otaIndexFile?.url,
                        fileIndexUrl: this.androidConf.fileIndexFile?.url,
                        releaseNotes : this.conf.releaseNotes,
                        fileSize : projectHelper.getHumanReadableSize(fileSize),
                        releaseMailFlags : conf.releaseMailFlags,
                        rb :rb
                    ]
            def result = engine.createTemplate(mailTemplate).make(binding)
            conf.mailMessageFile.location.write(result.toString(), "utf-8")
            logger.lifecycle("Mail message file created: ${conf.mailMessageFile}")
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareAvailableArtifactsInfo, project.prepareForRelease)
        project.sendMailMessage.dependsOn(task)
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name : "The file index file: ${conf.projectName}",
                url : new URL(conf.baseUrl, "${otaFolderPrefix}/file_index.html"),
                location : new File(conf.otaDirectory,"${otaFolderPrefix}/file_index.html"))
        fileIndexFile.location.parentFile.mkdirs()
        fileIndexFile.location.delete()
        androidConf.fileIndexFile = fileIndexFile
    }

    private preparePlainFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact plainFileIndexFile = new AmebaArtifact(
                name : "The plain file index file: ${conf.projectName}",
                url : new URL(conf.baseUrl, "${otaFolderPrefix}/plain_file_index.html"),
                location : new File(conf.otaDirectory,"${otaFolderPrefix}/plain_file_index.html"))
        plainFileIndexFile.location.parentFile.mkdirs()
        plainFileIndexFile.location.delete()
        androidConf.plainFileIndexFile = plainFileIndexFile
    }

    private void prepareFileIndexFile(Project project, Collection<String> variants) {
        URL fileIndexTemplate = this.class.getResource("file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                    this.class.package.name + ".file_index",
                conf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                    baseUrl: androidConf.fileIndexFile.url,
                    title: conf.projectName,
                    variants: variants,
                    apkFiles: androidConf.apkFiles,
                    version: conf.fullVersionString,
                    currentDate: conf.buildDate,
                    androidConf: androidConf,
                    conf : conf,
                    rb : rb
                ]
        def result = engine.createTemplate(fileIndexTemplate).make(binding)
        androidConf.fileIndexFile.location.write(result.toString(), "utf-8")
        logger.lifecycle("File index created: ${androidConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile(Project project, Collection<String> variants) {
        URL plainFileIndexTemplate = this.class.getResource("plain_file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                    this.class.package.name + ".plain_file_index",
                conf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                    baseUrl: androidConf.plainFileIndexFile.url,
                    title: conf.projectName,
                    apkFiles: androidConf.apkFiles,
                    version: conf.fullVersionString,
                    currentDate: conf.buildDate,
                    androidConf: androidConf,
                    conf : conf,
                    rb: rb
                ]
        def result = engine.createTemplate(plainFileIndexTemplate).make(binding)
        androidConf.plainFileIndexFile.location.write(result.toString(), "utf-8")
        logger.lifecycle("Plain file index created: ${androidConf.plainFileIndexFile}")
    }

    private void prepareOtaIndexFile(Project project, Collection<String> variants) {
        String otaFolderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name : "The ota index file: ${conf.projectName}",
                url : new URL(conf.baseUrl, "${otaFolderPrefix}/index.html"),
                location : new File(conf.otaDirectory,"${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource("index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".index",
                conf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
        def binding = [
                    baseUrl: otaIndexFile.url,
                    title: conf.projectName,
                    androidConf: androidConf,
                    version : conf.fullVersionString,
                    releaseNotes: conf.releaseNotes,
                    currentDate: conf.buildDate,
                    iconFileName: conf.iconFile.name,
                    conf: conf,
                    rb : rb
                ]
        def result = engine.createTemplate(otaIndexTemplate).make(binding)
        otaIndexFile.location.write(result.toString(), "utf-8")
        androidConf.otaIndexFile = otaIndexFile
        logger.lifecycle("Ota index created: ${otaIndexFile}")
        project.ant.copy(file: conf.iconFile, tofile: new File(otaIndexFile.location.parentFile, conf.iconFile.name))
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

    void prepareUpdateVersionTask(Project project) {
        def task = project.task('updateVersion')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task.description = """Updates version stored in manifest file of the project.
           Numeric version is (incremented), String version is set from version.string property"""
        task << {
            use (PropertyCategory) {
                conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
                manifestHelper.updateVersion(project.rootDir, conf)
                logger.lifecycle("New version code: ${conf.versionCode}")
                logger.lifecycle("Updated version string to ${conf.versionString}")
                logger.lifecycle("Configuration : ${conf}")
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
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

Note that you need to load generic 'ameba-project-release' plugin before this plugin is loaded.
"""

}
