package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.release.tasks.AbstractPrepareMailMessageTask

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.getHumanReadableSize

class PrepareMailMessageTask extends AbstractPrepareMailMessageTask {

    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void fillTemplate() {

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

        def result = createTemplate(loadTemplate(), binding)
        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')

        logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
    }

    String fileSize() {
        getHumanReadableSize((releaseConf as AndroidReleaseConfiguration).apkFiles[variantsConf.mainVariant].location.size())
    }
}
