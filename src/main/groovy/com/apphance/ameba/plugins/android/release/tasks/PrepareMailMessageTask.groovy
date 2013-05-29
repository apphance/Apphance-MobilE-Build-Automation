package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.release.tasks.AbstractPrepareMailMessageTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.getHumanReadableSize
import static com.google.common.base.Preconditions.checkNotNull
import static org.gradle.api.logging.Logging.getLogger

class PrepareMailMessageTask extends AbstractPrepareMailMessageTask {

    private l = getLogger(getClass())

    @Inject AndroidVariantsConfiguration variantsConf

    @TaskAction
    void mailMessage() {

        checkNotNull(releaseConf?.mailMessageFile?.location?.parentFile)

        validateReleaseNotes(releaseConf.releaseNotes)

        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()

        def rb = loadBundle()
        releaseConf.releaseMailSubject = fillMailSubject(rb)

        def binding = [
                title: conf.projectName.value,
                version: conf.versionString,
                currentDate: releaseConf.buildDate,
                otaUrl: releaseConf.otaIndexFile?.url,
                fileIndexUrl: releaseConf.fileIndexFile?.url,
                releaseNotes: releaseConf.releaseNotes,
                fileSize: fileSize(),
                releaseMailFlags: releaseConf.releaseMailFlags,
                rb: rb
        ]

        def mailTemplate = loadTemplate()
        def result = createTemplate(mailTemplate, binding)

        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')

        l.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
    }

    String fileSize() {
        getHumanReadableSize((releaseConf as AndroidReleaseConfiguration).apkFiles[variantsConf.mainVariant].location.size())
    }
}
