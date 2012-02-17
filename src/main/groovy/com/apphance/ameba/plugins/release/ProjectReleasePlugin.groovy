package com.apphance.ameba.plugins.release;


import java.util.LinkedList

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ImageNameFilter
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin;
import com.apphance.ameba.plugins.release.PrepareReleaseSetupTask;
import com.apphance.ameba.plugins.release.ProjectReleaseProperty;
import com.apphance.ameba.plugins.release.VerifyReleaseSetupTask;
import com.apphance.ameba.vcs.plugins.git.GitPlugin
import com.apphance.ameba.vcs.plugins.mercurial.MercurialPlugin


/**
 * Plugin for Mercurial implementation of VCS system
 *
 */
class ProjectReleasePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(ProjectReleasePlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf

    void apply(Project project) {
        ProjectHelper.checkAnyPluginIsLoaded(project, this.class, AndroidPlugin.class, IOSPlugin.class)
        ProjectHelper.checkAnyPluginIsLoaded(project, this.class, MercurialPlugin.class, GitPlugin.class)
        projectHelper = new ProjectHelper()
        use (PropertyCategory) {
            conf = project.getProjectConfiguration()
        }
        prepareMailConfiguration(project)
        preparePrepareForReleaseTask(project)
        prepareVerifyReleaseNotesTask(project)
        prepareImageMontageTask(project)
        prepareSendMailMessageTask(project)
        prepareCleanReleaseTask(project)
        prepareSourcesZipTask(project)
        project.task('verifyReleaseSetup', type: VerifyReleaseSetupTask.class)
        project.task('prepareReleaseSetup', type: PrepareReleaseSetupTask.class)
        project.task('showReleaseSetup', type: ShowReleaseSetupTask.class)
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
        use (PropertyCategory) {
            conf.releaseMailFrom = project.readExpectedProperty(ProjectReleaseProperty.RELEASE_MAIL_FROM)
            conf.releaseMailTo = project.readExpectedProperty(ProjectReleaseProperty.RELEASE_MAIL_TO)
            conf.releaseMailFlags = []
            String flags = project.readProperty(ProjectReleaseProperty.RELEASE_MAIL_FLAGS)
            if (flags != null){
                conf.releaseMailFlags = flags.tokenize(",").collect { it.trim() }
            }
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

    def void preparePrepareForReleaseTask(Project project) {
        def task = project.task('prepareForRelease')
        task.group= AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task.description = "Prepares project for release"
        task << {
            prepareSourcesAndDocumentationArtifacts()
            prepareMailArtifacts(project)
            prepareGalleryArtifacts()
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareVerifyReleaseNotesTask(Project project) {
        def task = project.task('verifyReleaseNotes')
        task.group= AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task.description = "Verifies that release notes are set for the build"
        task << {
            if (conf.releaseNotes == null) {
                throw new GradleException("""Release notes of the project have not been set.... Please enter non-empty notes!\n
Either as -Prelease.notes='NOTES' gradle property or by setting RELEASE_NOTES environment variable""")
            }
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease)
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
        task.dependsOn(project.readProjectConfiguration, project.clean)
    }

    def void prepareImageMontageTask(Project project) {
        def task = project.task('prepareImageMontage')
        task.description = "Builds montage of images found in the project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            Collection<String>  command = new LinkedList<String>()
            command << "montage"
            ImageNameFilter imageFilter = new ImageNameFilter();
            project.rootDir.traverse([type: FileType.FILES, maxDepth : 20]) { file ->
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
            try {
                projectHelper.executeCommand(project,convertCommand)
                def imageMontageFileArtifact = new AmebaArtifact(
                        name : "Image Montage",
                        url : new URL(conf.versionedApplicationUrl, "${imageMontageFile.name}"),
                        location : imageMontageFile)
                conf.imageMontageFile = imageMontageFileArtifact
            } catch (Exception e) {
                logger.lifecycle("The convert binary execution failed: skipping image montage preparation. Add convert (ImageMagick) binary to the path to get image montage.")
            }
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease)
    }

    private void prepareSendMailMessageTask(Project project) {
        def task = project.task('sendMailMessage')
        task.description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode,imageMontage"""
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE

        task << {
            use (PropertyCategory) {
                def mailServer = project.readPropertyOrEnvironmentVariable("mail.server")
                def mailPort = project.readPropertyOrEnvironmentVariable("mail.port")
                ProjectConfiguration conf = project.getProjectConfiguration()
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
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease,project.verifyReleaseNotes)
    }

    def void prepareSourcesZipTask(Project project) {
        def task = project.task('buildSourcesZip')
        task.description = "Builds sources .zip file."
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << {
            File destZip = conf.sourcesZip.location
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
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease)
    }
}