package com.apphance.ameba.android.plugins.release

import com.apphance.ameba.PluginHelper
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.*
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import com.apphance.ameba.util.file.FileManager
import groovy.text.SimpleTemplateEngine
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileDownloader.downloadFile

/**
 * Plugin that provides release functionality for android.
 *
 */
class AndroidReleasePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(AndroidReleasePlugin.class)

    Project project
    ProjectHelper projectHelper
    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    AndroidProjectConfiguration androidConf
    AndroidReleaseConfiguration androidReleaseConf
    AndroidManifestHelper manifestHelper
    AndroidEnvironment androidEnvironment

    @Override
    public void apply(Project project) {
        androidEnvironment = new AndroidEnvironment(project)
        PluginHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class, ProjectReleasePlugin.class)
        use(PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.androidReleaseConf = AndroidReleaseConfigurationRetriever.getAndroidReleaseConfiguration(project)
        }
        use(ProjectReleaseCategory) {
            this.releaseConf = project.getProjectReleaseConfiguration()
        }
        this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        this.manifestHelper = new AndroidManifestHelper()
        prepareUpdateVersionTask(project)
        prepareBuildDocumentationZipTask(project)
        prepareAvailableArtifactsInfoTask(project)
        prepareMailMessageTask(project)
        def listener
        def builder
        AndroidSingleVariantApkBuilder.buildListeners << new AndroidReleaseApkListener(project, project.ant)
        AndroidSingleVariantJarBuilder.buildListeners << new AndroidReleaseJarListener(project, project.ant)
    }

    def void prepareBuildDocumentationZipTask(Project project) {
        def task = project.task('buildDocumentationZip')
        task.description = "Builds documentation .zip file."
        task.group = AMEBA_RELEASE
        task << {
            File destZip = releaseConf.documentationZip.location
            destZip.mkdirs()
            destZip.delete()
            File javadocDir = project.file("build/docs/javadoc")
            project.ant.zip(destfile: destZip, basedir: javadocDir)
            logger.lifecycle("Zipped documentation to ${destZip}")
        }
        task.dependsOn(project.javadoc, project.readProjectConfiguration, project.prepareForRelease)
    }

    private void prepareAvailableArtifactsInfoTask(Project project) {
        def task = project.task('prepareAvailableArtifactsInfo')
        task.description = "Prepares information about available artifacts for mail message to include"
        task.group = AMEBA_RELEASE
        def listener
        def builder
        if (androidEnvironment.isLibrary()) {
            builder = new AndroidSingleVariantJarBuilder(project, androidConf)
            listener = new AndroidReleaseJarListener(project, project.ant)
        } else {
            builder = new AndroidSingleVariantApkBuilder(project, androidConf)
            listener = new AndroidReleaseApkListener(project, project.ant)
        }
        task << {
            if (builder.hasVariants()) {
                androidConf.variants.each { variant ->
                    listener.buildArtifactsOnly(project, variant)
                }
            } else {
                listener.buildArtifactsOnly(project, 'Debug', 'Debug')
                listener.buildArtifactsOnly(project, 'Release', 'Release')
            }
            if (conf.versionString != null) {
                String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
                prepareFileIndexArtifact(otaFolderPrefix)
                preparePlainFileIndexArtifact(otaFolderPrefix)
                prepareOtaIndexFile(project)
                prepareFileIndexFile(androidConf.variants)
                preparePlainFileIndexFile()
            } else {
                logger.lifecycle("Skipping building artifacts -> the build is not versioned")
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
    }

    private void prepareMailMessageTask(Project project) {
        def task = project.task('prepareMailMessage')
        task.description = "Prepares mail message which summarises the release"
        task.group = AMEBA_RELEASE
        task << {
            releaseConf.mailMessageFile.location.parentFile.mkdirs()
            releaseConf.mailMessageFile.location.delete()
            logger.lifecycle("Variants: ${androidConf.variants}")
            URL mailTemplate = this.class.getResource("mail_message.html")
            def mainBuild = "${androidConf.mainVariant}"
            logger.lifecycle("Main build used for size calculation: ${mainBuild}")
            def fileSize = androidReleaseConf.apkFiles[mainBuild].location.size()
            ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".mail_message",
                    releaseConf.locale, this.class.classLoader)
            ProjectReleaseCategory.fillMailSubject(project, rb)
            SimpleTemplateEngine engine = new SimpleTemplateEngine()
            def binding = [
                    title: conf.projectName,
                    version: conf.fullVersionString,
                    currentDate: releaseConf.buildDate,
                    otaUrl: androidReleaseConf.otaIndexFile?.url,
                    fileIndexUrl: androidReleaseConf.fileIndexFile?.url,
                    releaseNotes: releaseConf.releaseNotes,
                    fileSize: FileManager.getHumanReadableSize(fileSize),
                    releaseMailFlags: releaseConf.releaseMailFlags,
                    rb: rb
            ]
            def result = engine.createTemplate(mailTemplate).make(binding)
            releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
            logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareAvailableArtifactsInfo,
                project.prepareForRelease)
        project.sendMailMessage.dependsOn(task)
    }

    private prepareFileIndexArtifact(String otaFolderPrefix) {
        AmebaArtifact fileIndexFile = new AmebaArtifact(
                name: "The file index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/file_index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/file_index.html"))
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

    private void prepareFileIndexFile(Collection<String> variants) {
        URL fileIndexTemplate = this.class.getResource("file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                    this.class.package.name + ".file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
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
        logger.lifecycle("File index created: ${androidReleaseConf.fileIndexFile}")
    }

    private void preparePlainFileIndexFile() {
        URL plainFileIndexTemplate = this.class.getResource("plain_file_index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                    this.class.package.name + ".plain_file_index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
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
        logger.lifecycle("Plain file index created: ${androidReleaseConf.plainFileIndexFile}")
    }

    private void prepareOtaIndexFile(Project project) {
        String otaFolderPrefix = "${releaseConf.projectDirectoryName}/${conf.fullVersionString}"
        AmebaArtifact otaIndexFile = new AmebaArtifact(
                name: "The ota index file: ${conf.projectName}",
                url: new URL(releaseConf.baseUrl, "${otaFolderPrefix}/index.html"),
                location: new File(releaseConf.otaDirectory, "${otaFolderPrefix}/index.html"))
        otaIndexFile.location.mkdirs()
        otaIndexFile.location.delete()
        URL otaIndexTemplate = this.class.getResource("index.html")
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".index",
                releaseConf.locale, this.class.classLoader)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        engine.verbose = logger.debugEnabled
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
        otaIndexFile.location.write(result.toString(), "utf-8")
        androidReleaseConf.otaIndexFile = otaIndexFile
        logger.lifecycle("Ota index created: ${otaIndexFile}")
        project.ant.copy(file: releaseConf.iconFile, tofile: new File(otaIndexFile.location.parentFile,
                releaseConf.iconFile.name))
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

    void prepareUpdateVersionTask(Project project) {
        def task = project.task('updateVersion')
        task.group = AMEBA_RELEASE
        task.description = """Updates version stored in manifest file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""
        task << {
            use(PropertyCategory) {
                conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
                conf.versionCode = project.readOptionalPropertyOrEnvironmentVariable('version.code') as Long
                manifestHelper.updateVersion(project.rootDir, conf.versionCode, conf.versionString)
                logger.lifecycle("New version code: ${conf.versionCode}")
                logger.lifecycle("Updated version string to ${conf.versionString}")
                logger.lifecycle("Configuration : ${conf}")
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
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
