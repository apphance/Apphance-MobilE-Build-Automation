package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.release.IOSReleaseConfiguration
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.util.file.FileManager
import groovy.text.SimpleTemplateEngine
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static com.apphance.ameba.plugins.ios.release.IOSReleaseConfigurationRetriever.getIosReleaseConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static org.gradle.api.logging.Logging.getLogger

class PrepareMailMessageTask {

    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf
    private IOSProjectConfiguration iosConf
    private IOSReleaseConfiguration iosReleaseConf

    private l = getLogger(getClass())

    PrepareMailMessageTask(Project project) {
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
        this.iosConf = getIosProjectConfiguration(project)
        this.iosReleaseConf = getIosReleaseConfiguration(project)
    }

    void prepareMailMessage() {
        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
        l.lifecycle("Targets: ${iosConf.targets}")
        l.lifecycle("Configurations: ${iosConf.configurations}")
        URL mailTemplate = this.class.getResource('mail_message.html')
        def fileSize = 0
        def existingBuild = iosReleaseConf.distributionZipFiles.find {
            it.value.location != null
        }
        if (existingBuild) {
            l.lifecycle("Main build used for size calculation: ${existingBuild.key}")
            fileSize = existingBuild.value.location.size()
        }
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".mail_message",
                releaseConf.locale, this.class.classLoader)
        ProjectReleaseCategory.fillMailSubject(conf, releaseConf, rb)
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
        l.lifecycle("Runnning template with $binding")
        if (iosReleaseConf.dmgImageFiles.size() > 0) {
            iosConf.families.each { family ->
                if (iosReleaseConf.dmgImageFiles["${family}-${iosConf.mainTarget}"] == null) {
                    throw new GradleException("Wrongly configured family or target: ${family}-${iosConf.mainTarget} missing")
                }
            }
        }
        def result = engine.createTemplate(mailTemplate).make(binding)
        releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
    }
}
