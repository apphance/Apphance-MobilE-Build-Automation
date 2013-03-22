package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.release.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.util.file.FileManager
import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.plugins.android.release.AndroidReleaseConfigurationRetriever.getAndroidReleaseConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class MailMessageTask {

    private l = getLogger(getClass())
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf
    private AndroidProjectConfiguration androidConf
    private AndroidReleaseConfiguration androidReleaseConf

    MailMessageTask(Project project) {
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
        this.androidConf = getAndroidProjectConfiguration(project)
        this.androidReleaseConf = getAndroidReleaseConfiguration(project)
    }

    public void mailMessage() {
        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
        l.lifecycle("Variants: ${androidConf.variants}")
        URL mailTemplate = this.class.getResource('mail_message.html')
        def mainBuild = "${androidConf.mainVariant}"
        l.lifecycle("Main build used for size calculation: ${mainBuild}")
        def fileSize = androidReleaseConf.apkFiles[mainBuild].location.size()
        ResourceBundle rb = getBundle("${this.class.package.name}.mail_message",
                releaseConf.locale, this.class.classLoader)
        ProjectReleaseCategory.fillMailSubject(conf, releaseConf, rb)
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
        l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
    }
}
