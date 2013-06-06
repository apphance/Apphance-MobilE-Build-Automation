package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.plugins.release.tasks.AbstractPrepareMailMessageTask
import org.gradle.api.GradleException

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.FAMILIES
import static com.apphance.ameba.util.file.FileManager.getHumanReadableSize

class PrepareMailMessageTask extends AbstractPrepareMailMessageTask {

    @Inject IOSConfiguration conf

    @Override
    void fillTemplate() {
        def fileSize = 0
        def existingBuild = ((IOSReleaseConfiguration) releaseConf).distributionZipFiles.find {
            it.value.location != null
        }
        if (existingBuild) {
            logger.lifecycle("Main build used for size calculation: ${existingBuild.key}")
            fileSize = existingBuild.value.location.size()
        }
        def rb = loadBundle()
        releaseConf.releaseMailSubject = fillMailSubject(rb)

        def dmgImgFiles = ((IOSReleaseConfiguration) releaseConf).dmgImageFiles

        def binding = [
                title: conf.projectName.value,
                version: conf.fullVersionString,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                installable: dmgImgFiles,
                mainTarget: conf.iosVariantsConf.mainVariant.target,
                families: FAMILIES,
                fileSize: getHumanReadableSize(fileSize),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]
        if (dmgImgFiles.size() > 0) {
            FAMILIES.each { family ->
                if (dmgImgFiles["${family}-${conf.iosVariantsConf.mainVariant.target}"] == null) {
                    throw new GradleException("Wrongly configured family or target: ${family}-${conf.iosVariantsConf.mainVariant.target} missing")
                }
            }
        }
        def mailTemplate = loadTemplate()
        def result = createTemplate(mailTemplate, binding)
        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')

        logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile.location}")
    }
}
