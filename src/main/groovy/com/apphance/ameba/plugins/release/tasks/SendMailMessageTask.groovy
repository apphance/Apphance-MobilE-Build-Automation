package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ReleaseConfiguration
import org.apache.tools.ant.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class SendMailMessageTask extends DefaultTask {

    static String NAME = 'sendMailMessage'
    String group = AMEBA_RELEASE
    String description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode,imageMontage"""

    @Inject
    private ReleaseConfiguration releaseConf

    @TaskAction
    void sendMailMessage() {
        def mailServer = releaseConf.mailServer
        def mailPort = releaseConf.mailPort

        Properties props = System.getProperties()
        props.put('mail.smtp.host', mailServer)
        props.put('mail.smtp.port', mailPort)

        project.configurations.mail.each {
            Project.class.classLoader.addURL(it.toURI().toURL())
        }

        ant.mail(
                mailhost: mailServer,
                mailport: mailPort,
                subject: releaseConf.releaseMailSubject,
                charset: 'utf-8',
                tolist: releaseConf.releaseMailTo) {
            from(address: releaseConf.releaseMailFrom)
            message(mimetype: "text/html", releaseConf.mailMessageFile.location.text)
            if (releaseConf.releaseMailFlags.value.contains("qrCode")) {
                fileset(file: releaseConf.QRCodeFile.location)
            }
            if (releaseConf.releaseMailFlags.value.contains("imageMontage") && releaseConf.imageMontageFile != null) {
                fileset(file: releaseConf.imageMontageFile.location)
            }
        }
    }
}
