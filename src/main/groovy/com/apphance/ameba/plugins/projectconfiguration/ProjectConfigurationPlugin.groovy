package com.apphance.ameba.plugins.projectconfiguration;


import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.logging.LogLevel

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ImageNameFilter
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyManager;
import com.apphance.ameba.plugins.release.PrepareReleaseSetupTask;
import com.apphance.ameba.plugins.release.ProjectReleaseProperty;
import com.apphance.ameba.plugins.release.VerifyReleaseSetupTask;


/**
 * Plugin for Mercurial implementation of VCS system
 *
 */
class ProjectConfigurationPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(ProjectConfigurationPlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf

    void apply(Project project) {
        projectHelper = new ProjectHelper()
        prepareMailConfiguration(project)
        prepareRepositories(project)
        prepareVerifySetupTask(project)
        readProjectConfigurationTask(project)
        project.task('verifyBaseSetup', type: VerifyBaseSetupTask.class)
        preparePrepareSetupTask(project)
        project.task('prepareBaseSetup', type: PrepareBaseSetupTask.class)
        project.task('checkTests', type: CheckTestsTask.class)
        showProjectConfigurationTask(project)
        project.task('verifyReleaseSetup', type: VerifyReleaseSetupTask.class)
        prepareVerifyReleaseNotesTask(project)
        prepareImageMontageTask(project)
        prepareSendMailMessageTask(project)
        prepareCleanReleaseTask(project)
        prepareCleanConfigurationTask(project)
        prepareCopyGalleryFilesTask(project)
        prepareSourcesZipTask(project)
        prepareShowPropertiesTask(project)
        prepareShowBasePropertiesTask(project)
        prepareShowReleasePropertiesTask(project)
        project.task('prepareReleaseSetup', type: PrepareReleaseSetupTask.class)
    }

    private prepareShowBasePropertiesTask(Project project) {
        def task =  project.task('showBaseProperties')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task.description = 'Prints all base properties'
        task.dependsOn(project.readProjectConfiguration)
        project.showProperties.dependsOn(task)
        task << {
            System.out.println(PropertyManager.listPropertiesAsString(project, ProjectBaseProperty.class, true))
        }
    }


    private prepareShowReleasePropertiesTask(Project project) {
        def task =  project.task('showReleaseProperties')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task.description = 'Prints all release properties'
        task.dependsOn(project.readProjectConfiguration)
        project.showProperties.dependsOn(task)
        task << {
            System.out.println(PropertyManager.listPropertiesAsString(project, ProjectReleaseProperty.class, true))
        }
    }


    void prepareMailConfiguration(Project project) {
        project.configurations.add('mail')
        project.dependencies.add('mail','org.apache.ant:ant-javamail:1.8.1')
        project.dependencies.add('mail','javax.mail:mail:1.4')
        project.dependencies.add('mail','javax.activation:activation:1.1.1')
    }

    void prepareRepositories(Project project) {
        project.repositories.mavenCentral()
    }

    void prepareVerifySetupTask(Project project) {
        def task = project.task('verifySetup')
        task.description = "Verifies if the project can be build properly"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task << {
            // this task does nothing. It is there to serve as umbrella task for other setup tasks
        }
    }

    def void preparePrepareSetupTask(Project project) {
        def task = project.task('prepareSetup')
        task.description = "Walk-throug wizard for preparing project's configuration"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        project.logging.setLevel(LogLevel.QUIET)
        task.logging.setLevel(LogLevel.QUIET)
        task << {
            // this task does nothing. It is there to serve as umbrella task for other setup tasks
        }
    }

    def void readProjectConfigurationTask(Project project) {
        def task = project.task('readProjectConfiguration')
        task.description = "Reads project's configuration and sets it up in projectConfiguration property of project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            this.conf = projectHelper.getProjectConfiguration(project)
            // NOTE! conf.versionString and conf.versionCode need to
            // be read before project configuration task -> task reading the version
            // should be injected here
            projectHelper.readBasicProjectData(project)
            prepareGeneratedDirectories(project)
            prepareSourcesAndDocumentationArtifacts()
            prepareMailArtifacts(project)
            prepareGalleryArtifacts()
        }
    }

    private prepareGeneratedDirectories(Project project) {
        conf.otaDirectory = new File(project.rootDir,"ota/")
        conf.tmpDirectory = new File(project.rootDir,"tmp/")
    }

    private prepareSourcesAndDocumentationArtifacts() {
        def sourceZipName = conf.projectVersionedName + "-src.zip"
        conf.sourcesZip = new AmebaArtifact(
                name : conf.projectName + "-src",
                url : null, // we do not publish
                location : new File(conf.tmpDirectory,sourceZipName))
        def documentationZipName = conf.projectVersionedName + "-doc.zip"
        conf.documentationZip = new AmebaArtifact(
                name : conf.projectName + "-doc",
                url : null,
                location : new File(conf.tmpDirectory,documentationZipName))
        conf.targetDirectory.mkdirs()
    }

    private prepareMailArtifacts(Project project) {
        conf.mailMessageFile = new AmebaArtifact(
                name : "Mail message file",
                url : new URL(conf.versionedApplicationUrl, "message_file.html"),
                location : new File(conf.targetDirectory,"message_file.html"))
        conf.releaseMailFrom = projectHelper.getExpectedProperty(project, "release.mail.from")
        conf.releaseMailTo = projectHelper.getExpectedProperty(project, "release.mail.to")
        conf.releaseMailFlags = []
        if (project.hasProperty('release.mail.flags')){
            String flags = project['release.mail.flags']
            conf.releaseMailFlags = flags.tokenize(",").collect { it.trim() }
        }
    }

    private prepareGalleryArtifacts() {
        conf.galleryCss = new AmebaArtifact(
                name : "CSS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_css/jquery.swipegallery.css"),
                location : new File(conf.targetDirectory, "_css/jquery.swipegallery.css"))
        conf.galleryJs = new AmebaArtifact(
                name : "JS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_res/jquery.swipegallery.js"),
                location : new File(conf.targetDirectory, "_res/jquery.swipegallery.js"))
        conf.galleryTrans = new AmebaArtifact(
                name : "JS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_res/trans.png"),
                location : new File(conf.targetDirectory, "_res/trans.png"))
    }

    def void showProjectConfigurationTask(Project project) {
        def task = project.task('showProjectConfiguration')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = "Shows project's configuration"
        task << {
            logger.lifecycle( "Configuration: " + project['project.configuration'])
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    def void prepareVerifyReleaseNotesTask(Project project) {
        def task = project.task('verifyReleaseNotes')
        task.group= AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = "Verifies that release notes are set for the build"
        task << {
            ProjectConfiguration conf = projectHelper.getProjectConfiguration(project)
            if (conf.releaseNotes == null) {
                throw new GradleException("""Release notes of the project have not been set.... Please enter non-empty notes!\n
Either as -Prelease.notes='NOTES' gradle property or by setting RELEASE_NOTES environment variable""")
            }
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareCleanConfigurationTask(Project project) {
        def task = project.task('cleanConfiguration')
        task.description = "Cleans configuration before each build"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            conf.buildDirectory.deleteDir()
            conf.tmpDirectory.deleteDir()
            conf.logDirectory.deleteDir()
            conf.buildDirectory.mkdirs()
            conf.logDirectory.mkdirs()
            conf.tmpDirectory.mkdirs()
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareCleanReleaseTask(Project project) {
        def task = project.task('cleanRelease')
        task.description = "Cleans release related directrories"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            conf.otaDirectory.deleteDir()
            conf.tmpDirectory.deleteDir()
            conf.otaDirectory.mkdirs()
            conf.tmpDirectory.mkdirs()
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareImageMontageTask(Project project) {
        def task = project.task('prepareImageMontage')
        task.description = "Builds montage of images found in the project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_REPORTS
        task << {
            Collection<String>  command = new LinkedList<String>()
            command << "montage"
            ImageNameFilter imageFilter = new ImageNameFilter();
            project.rootDir.eachFileRecurse { file ->
                if (imageFilter.isValid(project.rootDir, file)) {
                    command << file
                }
            }
            def  tempFile = File.createTempFile("image_montage_${conf.projectName}",".png")
            command << tempFile.toString()
            projectHelper.executeCommand(project, command as String [])
            def  imageMontageFile = new File(conf.targetDirectory, "${conf.projectName}-${conf.fullVersionString}-image-montage.png")
            imageMontageFile.parentFile.mkdirs()
            imageMontageFile.delete()
            String[] convertCommand = [
                "/opt/local/bin/convert",
                tempFile,
                "-font",
                "helvetica",
                "-pointsize",
                "36",
                "-draw",
                "gravity southwest fill black text 0,12 '${conf.projectName} Version: ${conf.fullVersionString} Generated: ${conf.buildDate}'",
                imageMontageFile
            ]
            projectHelper.executeCommand(project,convertCommand)
            def imageMontageFileArtifact = new AmebaArtifact(
                    name : "Image Montage",
                    url : new URL(conf.versionedApplicationUrl, "${imageMontageFile.name}"),
                    location : imageMontageFile)
            conf.imageMontageFile = imageMontageFileArtifact
        }
        task.dependsOn("readProjectConfiguration")
    }

    private void prepareSendMailMessageTask(Project project) {
        def task = project.task('sendMailMessage')
        task.description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode,imageMontage"""
        task.group = AmebaCommonBuildTaskGroups.AMEBA_MESSAGING

        task << {
            def mailServer = projectHelper.readPropertyOrEnvironmentVariable(project,"mail.server")
            def mailPort = projectHelper.readPropertyOrEnvironmentVariable(project,"mail.port")
            ProjectConfiguration conf = projectHelper.getProjectConfiguration(project)
            Properties props = System.getProperties();
            props.put("mail.smtp.host", mailServer);
            props.put("mail.smtp.port", mailPort);
            project.configurations.mail.each {
                org.apache.tools.ant.Project.class.classLoader.addURL(it.toURI().toURL())
            }
            ant.mail(
                    mailhost: mailServer,
                    mailport: mailPort,
                    subject: conf.releaseMailSubject,
                    charset: "UTF-8",
                    tolist : conf.releaseMailTo){
                        from(address: conf.releaseMailFrom)
                        message(mimetype:"text/html", conf.mailMessageFile.location.text)
                        if (conf.releaseMailFlags.contains("qrCode")) {
                            fileset(file: conf.qrCodeFile.location)
                        }
                        if (conf.releaseMailFlags.contains("imageMontage") && conf.imageMontageFile != null) {
                            fileset(file: conf.imageMontageFile.location)
                        }
                    }
        }
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.verifyReleaseNotes)
    }

    def void prepareCopyGalleryFilesTask(Project project) {
        def task = project.task('copyGalleryFiles')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = "Copy files required by swipe jquerymobile gallery"
        task << {
            conf.galleryCss.location.parentFile.mkdirs()
            conf.galleryJs.location.parentFile.mkdirs()
            conf.galleryCss.location.setText(this.class.getResourceAsStream("swipegallery/_css/jquery.swipegallery.css").text,"utf-8")
            conf.galleryJs.location.setText(this.class.getResourceAsStream("swipegallery/_res/jquery.swipegallery.js").text,"utf-8")
            conf.galleryTrans.location.setText(this.class.getResourceAsStream("swipegallery/_res/trans.png").text,"utf-8")
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareSourcesZipTask(Project project) {
        def task = project.task('buildSourcesZip')
        task.description = "Builds sources .zip file."
        task.group = AmebaCommonBuildTaskGroups.AMEBA_REPORTS
        task << {
            File destZip = projectHelper.getProjectConfiguration(project).sourcesZip.location
            logger.lifecycle("Removing empty symlinks")
            projectHelper.removeMissingSymlinks(project.rootDir)
            destZip.parentFile.mkdirs()
            destZip.delete()
            logger.lifecycle("Compressing sources")
            ant.zip(destfile: destZip ) {
                fileset(dir: project.rootDir) {
                    exclude(name: "build/**")
                    exclude(name: "ota/**")
                    exclude(name: "tmp/**")
                    exclude(name: "**/buildSrc/build/**")
                    exclude(name: '**/build.gradle')
                    exclude(name: '**/gradle.properties')
                    exclude(name: '**/.gradle/**')
                    conf.sourceExcludes.each { exclude(name: it) }
                }
            }
            logger.lifecycle("Extra source excludes ${conf.sourceExcludes}")
            logger.lifecycle("Created source files at ${destZip}")
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    void prepareShowPropertiesTask(Project project) {
        def task = project.task('showProperties')
        task.description = "Shows all available project properties"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task.showComments = true
        task << {
            // this task does nothing. It is there to serve as umbrella task for other setup tasks
        }
    }
}