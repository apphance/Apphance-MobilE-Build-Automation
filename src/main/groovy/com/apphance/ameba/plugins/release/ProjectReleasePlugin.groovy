package com.apphance.ameba.plugins.release

import com.apphance.ameba.*
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.util.file.FileManager
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES

/**
 * Plugin for releasing projects.
 *
 */
class ProjectReleasePlugin implements Plugin<Project> {

    def l = Logging.getLogger(getClass())

    ProjectHelper projectHelper
    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf

    void apply(Project project) {
        PluginHelper.checkAnyPluginIsLoaded(project, getClass(), AndroidPlugin.class, IOSPlugin.class)
        projectHelper = new ProjectHelper()
        conf = PropertyCategory.getProjectConfiguration(project)
        releaseConf = ProjectReleaseCategory.retrieveProjectReleaseData(project)
        prepareMailConfiguration(project)
        prepareCopyGalleryFilesTask(project)
        preparePrepareForReleaseTask(project)
        prepareVerifyReleaseNotesTask(project)
        prepareImageMontageTask(project)
        prepareSendMailMessageTask(project)
        prepareCleanReleaseTask(project)
        prepareSourcesZipTask(project)
        project.prepareSetup.prepareSetupOperations << new PrepareReleaseSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyReleaseSetupOperation()
        project.showSetup.showSetupOperations << new ShowReleaseSetupOperation()
    }

    def prepareMailConfiguration(Project project) {
        project.configurations.add('mail')
        project.dependencies.add('mail', 'org.apache.ant:ant-javamail:1.8.1')
        project.dependencies.add('mail', 'javax.mail:mail:1.4')
        project.dependencies.add('mail', 'javax.activation:activation:1.1.1')
    }

    def prepareCopyGalleryFilesTask(Project project) {
        def task = project.task('copyGalleryFiles')
        task.group = AMEBA_CONFIGURATION
        task.description = 'Copy files required by swipe jquerymobile gallery'
        task << {
            prepareGalleryArtifacts()
            releaseConf.galleryCss.location.parentFile.mkdirs()
            releaseConf.galleryJs.location.parentFile.mkdirs()
            releaseConf.galleryCss.location.setText(getClass().getResourceAsStream('swipegallery/_css/jquery.swipegallery.css').text, 'utf-8')
            releaseConf.galleryJs.location.setText(getClass().getResourceAsStream('swipegallery/_res/jquery.swipegallery.js').text, 'utf-8')
            releaseConf.galleryTrans.location.setText(getClass().getResourceAsStream('swipegallery/_res/trans.png').text, 'utf-8')
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    private prepareGalleryArtifacts() {
        releaseConf.galleryCss = new AmebaArtifact(
                name: 'CSS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_css/jquery.swipegallery.css'),
                location: new File(releaseConf.targetDirectory, '_css/jquery.swipegallery.css'))
        releaseConf.galleryJs = new AmebaArtifact(
                name: 'JS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_res/jquery.swipegallery.js'),
                location: new File(releaseConf.targetDirectory, '_res/jquery.swipegallery.js'))
        releaseConf.galleryTrans = new AmebaArtifact(
                name: 'JS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_res/trans.png'),
                location: new File(releaseConf.targetDirectory, '_res/trans.png'))
    }

    def void preparePrepareForReleaseTask(Project project) {
        def task = project.task('prepareForRelease')
        task.group = AMEBA_RELEASE
        task.description = 'Prepares project for release'
        task << {
            prepareSourcesAndDocumentationArtifacts()
            prepareMailArtifacts(project)
        }
        task.dependsOn(project.readProjectConfiguration, project.copyGalleryFiles)
    }

    private prepareSourcesAndDocumentationArtifacts() {
        def sourceZipName = conf.projectVersionedName + "-src.zip"
        releaseConf.sourcesZip = new AmebaArtifact(
                name: conf.projectName + "-src",
                url: null, // we do not publish
                location: new File(conf.tmpDirectory, sourceZipName))
        def documentationZipName = conf.projectVersionedName + "-doc.zip"
        releaseConf.documentationZip = new AmebaArtifact(
                name: conf.projectName + "-doc",
                url: null,
                location: new File(conf.tmpDirectory, documentationZipName))
        releaseConf.targetDirectory.mkdirs()
    }

    private prepareMailArtifacts(Project project) {
        releaseConf.mailMessageFile = new AmebaArtifact(
                name: "Mail message file",
                url: new URL(releaseConf.versionedApplicationUrl, "message_file.html"),
                location: new File(releaseConf.targetDirectory, "message_file.html"))
        use(PropertyCategory) {
            releaseConf.releaseMailFrom = project.readExpectedProperty(ProjectReleaseProperty.RELEASE_MAIL_FROM)
            releaseConf.releaseMailTo = project.readExpectedProperty(ProjectReleaseProperty.RELEASE_MAIL_TO)
            releaseConf.releaseMailFlags = []
            String flags = project.readProperty(ProjectReleaseProperty.RELEASE_MAIL_FLAGS)
            if (flags != null) {
                releaseConf.releaseMailFlags = flags.tokenize(",").collect { it.trim() }
            }
        }
    }

    def void prepareVerifyReleaseNotesTask(Project project) {
        def task = project.task('verifyReleaseNotes')
        task.group = AMEBA_RELEASE
        task.description = 'Verifies that release notes are set for the build'
        task << {
            if (releaseConf.releaseNotes == null) {
                throw new GradleException("""Release notes of the project have not been set.... Please enter non-empty notes!\n
Either as -Prelease.notes='NOTES' gradle property or by setting RELEASE_NOTES environment variable""")
            }
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease)
    }

    def void prepareImageMontageTask(Project project) {
        def task = project.task('prepareImageMontage')
        task.description = 'Builds montage of images found in the project'
        task.group = AMEBA_RELEASE
        task << {
            Collection<String> command = new LinkedList<String>()
            command << 'montage'
            project.rootDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
                if (ImageNameFilter.isValid(project.rootDir, file)) {
                    command << file
                }
            }
            def tempFile = File.createTempFile("image_montage_${conf.projectName}", '.png')
            command << tempFile.toString()
            projectHelper.executeCommand(project, command as String[])
            def imageMontageFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${conf.fullVersionString}-image-montage.png")
            imageMontageFile.parentFile.mkdirs()
            imageMontageFile.delete()
            String[] convertCommand = [
                    '/opt/local/bin/convert',
                    tempFile,
                    '-font',
                    'helvetica',
                    '-pointsize',
                    '36',
                    '-draw',
                    "gravity southwest fill black text 0,12 '${conf.projectName} Version: ${conf.fullVersionString} Generated: ${releaseConf.buildDate}'",
                    imageMontageFile
            ]
            try {
                projectHelper.executeCommand(project, convertCommand)
                def imageMontageFileArtifact = new AmebaArtifact(
                        name: "Image Montage",
                        url: new URL(releaseConf.versionedApplicationUrl, "${imageMontageFile.name}"),
                        location: imageMontageFile)
                releaseConf.imageMontageFile = imageMontageFileArtifact
            } catch (Exception e) {
                l.lifecycle("The convert binary execution failed: skipping image montage preparation. Add convert (ImageMagick) binary to the path to get image montage.")
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
        task.group = AMEBA_RELEASE

        task << {
            use(PropertyCategory) {
                def mailServer = project.readPropertyOrEnvironmentVariable('mail.server')
                def mailPort = project.readPropertyOrEnvironmentVariable('mail.port')
                Properties props = System.getProperties();
                props.put("mail.smtp.host", mailServer);
                props.put("mail.smtp.port", mailPort);
                project.configurations.mail.each {
                    org.apache.tools.ant.Project.class.classLoader.addURL(it.toURI().toURL())
                }
                ant.mail(
                        mailhost: mailServer,
                        mailport: mailPort,
                        subject: releaseConf.releaseMailSubject,
                        charset: 'utf-8',
                        tolist: releaseConf.releaseMailTo) {
                    from(address: releaseConf.releaseMailFrom)
                    message(mimetype: "text/html", releaseConf.mailMessageFile.location.text)
                    if (releaseConf.releaseMailFlags.contains("qrCode")) {
                        fileset(file: releaseConf.qrCodeFile.location)
                    }
                    if (releaseConf.releaseMailFlags.contains("imageMontage") && releaseConf.imageMontageFile != null) {
                        fileset(file: releaseConf.imageMontageFile.location)
                    }
                }
            }
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease, project.verifyReleaseNotes)
    }

    def void prepareCleanReleaseTask(Project project) {
        def task = project.task('cleanRelease')
        task.description = 'Cleans release related directories'
        task.group = AMEBA_RELEASE
        task << {
            releaseConf.otaDirectory.deleteDir()
            conf.tmpDirectory.deleteDir()
            releaseConf.otaDirectory.mkdirs()
            conf.tmpDirectory.mkdirs()
        }
        task.dependsOn(project.readProjectConfiguration, project.clean)
    }

    def void prepareSourcesZipTask(Project project) {
        def task = project.task('buildSourcesZip')
        task.description = 'Builds sources .zip file.'
        task.group = AMEBA_RELEASE
        task << {
            File destZip = releaseConf.sourcesZip.location
            l.lifecycle('Removing empty symlinks')
            FileManager.removeMissingSymlinks(project.rootDir)
            destZip.parentFile.mkdirs()
            destZip.delete()
            l.lifecycle('Compressing sources')
            ant.zip(destfile: destZip) {
                fileset(dir: project.rootDir) {
                    exclude(name: 'build/**')
                    exclude(name: 'ota/**')
                    exclude(name: 'tmp/**')
                    exclude(name: '**/buildSrc/build/**')
                    exclude(name: '**/build.gradle')
                    exclude(name: '**/gradle.properties')
                    exclude(name: '**/.gradle/**')
                    conf.sourceExcludes.each { exclude(name: it) }
                }
            }
            l.lifecycle("Extra source excludes ${conf.sourceExcludes}")
            l.lifecycle("Created source files at $destZip")
        }
        task.dependsOn(project.readProjectConfiguration, project.prepareForRelease)
    }

    static public final String DESCRIPTION =
        """This is Ameba release plugin.

The plugin provides all the basic tasks required to prepare OTA release of
an application. It should be added after build plugin is added.
"""
}