package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.release.tasks.AbstractPrepareMailMessageTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.FAMILIES
import static com.apphance.ameba.util.file.FileManager.getHumanReadableSize
import static com.google.common.base.Preconditions.checkNotNull
import static org.gradle.api.logging.Logging.getLogger

class PrepareMailMessageTask extends AbstractPrepareMailMessageTask {

    private l = getLogger(getClass())

    @Inject IOSVariantsConfiguration variantsConf

    @TaskAction
    void mailMessage() {

        checkNotNull(releaseConf?.mailMessageFile?.location?.parentFile)
        validateReleaseNotes(releaseConf.releaseNotes)

        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()

        def fileSize = 0
        def existingBuild = conf().distributionZipFiles.find {
            it.value.location != null
        }
        if (existingBuild) {
            l.lifecycle("Main build used for size calculation: ${existingBuild.key}")
            fileSize = existingBuild.value.location.size()
        }
        def rb = loadBundle()
        releaseConf.releaseMailSubject = fillMailSubject(rb)

        def mainVariant = variantsConf.mainVariant

        def binding = [
                title: mainVariant.projectName,
                version: mainVariant.fullVersionString,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                installable: conf().dmgImageFiles,
                mainTarget: mainVariant.target,
                families: FAMILIES,
                fileSize: getHumanReadableSize(fileSize),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]
        if (conf().dmgImageFiles.size() > 0) {
            FAMILIES.each { family ->
                if (conf().dmgImageFiles["${family}-${mainVariant.target}"] == null) {
                    throw new GradleException("Wrongly configured family or target: ${family}-${mainVariant.target} missing")
                }
            }
        }
        def mailTemplate = loadTemplate()
        def result = createTemplate(mailTemplate, binding)
        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')

        l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile.location}")
    }

    private IOSReleaseConfiguration conf() {
        releaseConf as IOSReleaseConfiguration
    }
}
