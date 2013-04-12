package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.util.file.FileManager
import com.google.inject.Inject
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_MAIL_MESSAGE_TASK_NAME
import static com.google.common.base.Preconditions.checkNotNull
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class MailMessageTask extends DefaultTask {

    private l = getLogger(getClass())

    static String name = PREPARE_MAIL_MESSAGE_TASK_NAME
    String group = AMEBA_RELEASE
    String description = 'Prepares mail message which summarises the release'

    @Inject AndroidConfiguration androidConfiguration
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AndroidVariantsConfiguration variantsConf

    @TaskAction
    public void mailMessage() {
        checkNotNull(releaseConf?.mailMessageFile?.location?.parentFile)

        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()

        l.lifecycle("Variants: ${variantsConf.variants*.name}")
        URL mailTemplate = this.class.getResource('mail_message.html')
        def mainBuild = variantsConf.mainVariant
        l.lifecycle("Main build used for size calculation: ${mainBuild}")

        def fileSize = releaseConf.apkFiles[mainBuild].location.size()
        ResourceBundle rb = getBundle("${this.class.package.name}.mail_message", releaseConf.locale, this.class.classLoader)
        releaseConf.releaseMailSubject = ProjectReleaseCategory.fillMailSubject(androidConfiguration, rb)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [
                title: androidConfiguration.projectName.value,
                version: androidConfiguration.versionString.value,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                fileSize: FileManager.getHumanReadableSize(fileSize),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]
        def result = engine.createTemplate(mailTemplate).make(binding)
        releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
    }
}
