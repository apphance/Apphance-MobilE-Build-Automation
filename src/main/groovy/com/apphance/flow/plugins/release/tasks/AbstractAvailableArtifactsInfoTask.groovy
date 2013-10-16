package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.text.SimpleDateFormat

import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.getReleaseDirName
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static java.net.URLEncoder.encode
import static java.util.ResourceBundle.getBundle

abstract class AbstractAvailableArtifactsInfoTask extends DefaultTask {

    static final NAME = 'prepareAvailableArtifactsInfo'
    String group = FLOW_RELEASE
    String description = 'Generates release artifacts, this includes: mail message, QR code with installation link and installation HTML sites.'

    @Inject ProjectConfiguration conf
    @Inject ReleaseConfiguration releaseConf

    FlowArtifact mailMessageFile = new FlowArtifact()
    FlowArtifact fileIndexFile = new FlowArtifact()
    FlowArtifact plainFileIndexFile = new FlowArtifact()
    FlowArtifact otaIndexFile = new FlowArtifact()
    FlowArtifact QRCodeFile = new FlowArtifact()

    String versionString
    String fullVersionString
    String releaseUrl
    File releaseIcon
    Collection<String> releaseNotes = ['']
    String releaseMailFlags = 'qrCode,imageMontage'
    File rootDir = project.rootDir

    Closure<URL> releaseUrlVersioned = { new URL("$releaseUrl/$fullVersionString") }
    String projectName = project.name
    Closure<String> projectNameNoWhiteSpace = { projectName?.replaceAll('\\s', '_') }
    Closure<File> releaseDir = { new File(project.rootDir, OTA_DIR + "/${getReleaseDirName(releaseUrl)}/$fullVersionString") }
    String buildDate = new SimpleDateFormat("dd-MM-yyyy HH:mm zzz").format(new Date())

    @Inject
    void initTask() {
        releaseConf.mailMessageFile = mailMessageFile
        releaseConf.fileIndexFile = fileIndexFile
        releaseConf.plainFileIndexFile = plainFileIndexFile
        releaseConf.otaIndexFile = otaIndexFile
        releaseConf.QRCodeFile = QRCodeFile

        projectName = conf.projectName.value
        versionString = conf.versionString
        buildDate = releaseConf.buildDate
        fullVersionString = conf.fullVersionString
        releaseUrlVersioned = { releaseConf.releaseUrlVersioned }
        releaseDir = { releaseConf.releaseDir }
        releaseIcon = releaseConf.releaseIcon?.value
        releaseNotes = releaseConf.releaseNotes
        releaseMailFlags = releaseConf.releaseMailFlags
        rootDir = conf.rootDir
    }

    def engine = new SimpleTemplateEngine()

    @Lazy Map basicBinding = [
            title: projectName,
            version: versionString,
            currentDate: buildDate
    ]

    @TaskAction
    void availableArtifactsInfo() {

        createArtifactFile mailMessageFile, 'Mail message file', 'message_file.html'
        createArtifactFile fileIndexFile, "The file index file: ${projectName}", 'file_index.html'
        createArtifactFile plainFileIndexFile, "The plain file index file: ${projectName}", 'plain_file_index.html'
        createArtifactFile otaIndexFile, "The ota index file: ${projectName}", 'index.html'
        createArtifactFile QRCodeFile, 'QR Code', "${projectNameNoWhiteSpace()}-$fullVersionString-qrcode.png"

        prepareOtherArtifacts()

        prepareQRCode()
        prepareIconFile()
        prepareMailMsg()
        preparePlainFileIndexFile()
        prepareOTAIndexFile()
    }

    void createArtifactFile(FlowArtifact artifact, artifactName, artifactFileName) {
        artifact.name = artifactName
        artifact.url = new URL("${releaseUrlVersioned()}/$artifactFileName")
        artifact.location = new File(releaseDir(), artifactFileName)
        artifact.location.parentFile.mkdirs()
        artifact.location.delete()
    }

    abstract void prepareOtherArtifacts()

    void prepareQRCode() {
        def urlEncoded = encode(otaIndexFile.url.toString(), 'utf-8')
        def qr = new FlowUtils().downloadToTempFile("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=$urlEncoded")
        (new AntBuilder()).copy(file: qr.absolutePath, tofile: QRCodeFile.location.absolutePath)
        qr.delete()
        logger.lifecycle("QRCode created: ${QRCodeFile.location}")
    }

    void prepareIconFile() {
        ant.copy(file: releaseIcon.exists() ? releaseIcon : new File(rootDir, releaseIcon.path),
                tofile: new File(releaseDir(), releaseIcon.name))
    }

    void prepareMailMsg() {
        if (releaseConf) {
            releaseConf.releaseMailSubject = fillMailSubject()
            def result = fillTemplate(loadTemplate('mail_message.html'), mailMsgBinding())
            mailMessageFile.location.write(result.toString(), 'UTF-8')
            logger.lifecycle("Mail message file created: ${mailMessageFile.location}")
        }
    }

    String fillMailSubject() {
        ResourceBundle rb = bundle('mail_message')
        String subject = rb.getString('Subject')
        engine.createTemplate(subject).make projectName: projectName, fullVersionString: fullVersionString
    }

    abstract Map mailMsgBinding()

    void preparePlainFileIndexFile() {
        def result = fillTemplate(loadTemplate('plain_file_index.html'), plainFileIndexFileBinding())
        templateToFile(plainFileIndexFile.location, result)
        logger.lifecycle("Plain file index created: ${plainFileIndexFile.location}")
    }

    abstract Map plainFileIndexFileBinding()

    void prepareOTAIndexFile() {
        def result = fillTemplate(loadTemplate('index.html'), otaIndexFileBinding())
        templateToFile(otaIndexFile.location, result)
        logger.lifecycle("OTA index created: ${otaIndexFile.location}")
    }

    abstract Map otaIndexFileBinding()

    ResourceBundle bundle(String id) {
        def c = getClass()
        getBundle("${c.package.name}.$id", releaseConf?.locale ?: Locale.default, c.classLoader)
    }

    URL loadTemplate(String template) {
        getClass().getResource(template)
    }

    Writable fillTemplate(URL tmpl, Map binding) {
        engine.createTemplate(tmpl).make(binding)
    }

    void templateToFile(File f, Writable tmpl) {
        f.write(tmpl.toString(), 'UTF-8')
    }

    void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = [releaseNotes]
    }
}
