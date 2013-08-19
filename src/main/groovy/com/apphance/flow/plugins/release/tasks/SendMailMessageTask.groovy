package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.release.ReleaseConfiguration
import org.apache.tools.ant.Project
import org.gradle.api.AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.release.ReleaseConfiguration.*
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE

class SendMailMessageTask extends DefaultTask {

    static String NAME = 'sendMailMessage'
    String group = FLOW_RELEASE
    String description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode, imageMontage"""

    @Inject ReleaseConfiguration releaseConf
    @Inject AntBuilder ant

    @TaskAction
    void sendMailMessage() {

        validateMailServer(releaseConf.mailServer)
        validateMailPort(releaseConf.mailPort)

        validateMail(releaseConf.releaseMailFrom)
        validateMail(releaseConf.releaseMailTo)

        System.properties['mail.smtp.host'] = releaseConf.mailServer
        System.properties['mail.smtp.port'] = releaseConf.mailPort

        project.configurations.mail.each {
            Project.class.classLoader.addURL(it.toURI().toURL())
        }

        def attachments = []
        if (releaseConf.releaseMailFlags.value.contains('qrCode')) {
            attachments << releaseConf.QRCodeFile.location
        }
        if (releaseConf.releaseMailFlags.value.contains('imageMontage') && releaseConf.imageMontageFile != null) {
            attachments << releaseConf.imageMontageFile.location
        }

        ant.mail(
                mailhost: releaseConf.mailServer,
                mailport: releaseConf.mailPort,
                subject: releaseConf.releaseMailSubject,
                charset: 'UTF-8',
                tolist: releaseConf.releaseMailTo.value,
                from: releaseConf.releaseMailFrom.value,
                message: releaseConf.mailMessageFile?.location?.text,
                messageMimeType: 'text/html',
                files: attachments.toString()
        )
    }
}
