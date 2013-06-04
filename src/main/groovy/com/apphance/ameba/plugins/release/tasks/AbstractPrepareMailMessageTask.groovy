package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static java.util.ResourceBundle.getBundle

abstract class AbstractPrepareMailMessageTask extends DefaultTask {

    static String NAME = 'prepareMailMessage'
    String group = AMEBA_RELEASE
    String description = 'Prepares mail message which summarises the release'

    @Inject ProjectConfiguration conf
    @Inject ReleaseConfiguration releaseConf

    @TaskAction
    final void prepareMailMessage() {
        validateReleaseNotes(releaseConf.releaseNotes)
        prepareMailMsgArtifact()
        fillTemplate()
    }

    private void prepareMailMsgArtifact() {
        releaseConf.mailMessageFile = new AmebaArtifact(
                name: 'Mail message file',
                url: new URL(releaseConf.versionedApplicationUrl, 'message_file.html'),
                location: new File(releaseConf.targetDir, 'message_file.html'))
        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
    }

    abstract void fillTemplate()

    @PackageScope
    void validateReleaseNotes(Collection<String> releaseNotes) {
        if (!releaseNotes || releaseNotes.empty) {
            throw new GradleException("""|Release notes are empty!
                                         |Set them either by 'release.notes' system property or
                                         |'RELEASE_NOTES environment variable!""")
        }
    }

    @PackageScope
    ResourceBundle loadBundle() {
        getBundle("${getClass().package.name}.mail_message", releaseConf.locale, getClass().classLoader)
    }

    @PackageScope
    String fillMailSubject(ResourceBundle rb) {
        String subject = rb.getString('Subject')
        Eval.me("conf", conf, /"$subject"/)
    }

    @PackageScope
    URL loadTemplate() {
        getClass().getResource('mail_message.html')
    }

    @PackageScope
    Writable createTemplate(URL templateURL, Map binding) {
        def engine = new SimpleTemplateEngine()
        engine.createTemplate(templateURL).make(binding)
    }
}
