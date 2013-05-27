package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileManager.getHumanReadableSize
import static com.google.common.base.Preconditions.checkNotNull
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class PrepareMailMessageTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'prepareMailMessage'
    String group = AMEBA_RELEASE
    String description = 'Prepares mail message which summarises the release'

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AndroidVariantsConfiguration variantsConf

    @TaskAction
    public void mailMessage() {

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

    @groovy.transform.PackageScope
    void validateReleaseNotes(Collection<String> releaseNotes) {
        if (!releaseNotes || releaseNotes.empty) {
            throw new GradleException("""|Release notes are empty!
                                         |Set them either by 'release.notes' system property or
                                         |'RELEASE_NOTES environment variable!""")
        }
    }

    private ResourceBundle loadBundle() {
        getBundle("${getClass().package.name}.mail_message", releaseConf.locale, getClass().classLoader)
    }

    private String fillMailSubject(ResourceBundle rb) {
        String subject = rb.getString('Subject')
        Eval.me("conf", conf, /"$subject"/)
    }

    private String fileSize() {
        getHumanReadableSize(releaseConf.apkFiles[variantsConf.mainVariant].location.size())
    }

    private URL loadTemplate() {
        getClass().getResource('mail_message.html')
    }

    private Writable createTemplate(URL templateURL, Map binding) {
        def engine = new SimpleTemplateEngine()
        engine.createTemplate(templateURL).make(binding)
    }
}
