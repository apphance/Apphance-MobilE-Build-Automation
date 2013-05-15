package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.util.file.FileManager
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.FAMILIES
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static org.gradle.api.logging.Logging.getLogger

//TODO common super class with same Android's task
class PrepareMailMessageTask extends DefaultTask {

    static final String NAME = 'prepareMailMessage'
    String description = 'Prepares mail message which summarises the release'
    String group = AMEBA_RELEASE

    @Inject
    IOSConfiguration conf
    @Inject
    IOSReleaseConfiguration releaseConf
    @Inject
    IOSVariantsConfiguration variantsConf

    private l = getLogger(getClass())

    @TaskAction
    void prepareMailMessage() {
        def mainVariant = variantsConf.mainVariant

        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
        URL mailTemplate = this.class.getResource('mail_message.html')
        def fileSize = 0
        def existingBuild = releaseConf.distributionZipFiles.find {
            it.value.location != null
        }
        if (existingBuild) {
            l.lifecycle("Main build used for size calculation: ${existingBuild.key}")
            fileSize = existingBuild.value.location.size()
        }
        ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".mail_message",
                releaseConf.locale, this.class.classLoader)
        releaseConf.releaseMailSubject = fillMailSubject(rb)
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [
                title: mainVariant.projectName,
                version: mainVariant.fullVersionString,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                installable: releaseConf.dmgImageFiles,
                mainTarget: mainVariant.target,
                families: FAMILIES,
                fileSize: FileManager.getHumanReadableSize(fileSize),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]
        if (releaseConf.dmgImageFiles.size() > 0) {
            FAMILIES.each { family ->
                if (releaseConf.dmgImageFiles["${family}-${mainVariant.target}"] == null) {
                    throw new GradleException("Wrongly configured family or target: ${family}-${mainVariant.target} missing")
                }
            }
        }
        def result = engine.createTemplate(mailTemplate).make(binding)
        releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
        l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
    }

    private String fillMailSubject(ResourceBundle bundle) {
        String subject = bundle.getString('Subject')
        Eval.me("conf", conf, /"$subject"/)
    }
}
