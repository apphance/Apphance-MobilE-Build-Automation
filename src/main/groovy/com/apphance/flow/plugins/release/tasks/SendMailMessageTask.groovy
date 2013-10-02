package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.validation.ReleaseValidator
import org.apache.tools.ant.Project
import org.gradle.api.AntBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE

class SendMailMessageTask extends DefaultTask {

    static String NAME = 'sendMailMessage'
    String group = FLOW_RELEASE
    String description = "Sends mail message. Requires mail.server, mail.port system (-D) properties or " +
            "corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication). " +
            "It also uses certain properties to send mails: 'release.mail.from', " +
            "'release.mail.to' and 'release.mail.flags'. See configuration reference for more details."

    @Inject AntBuilder ant
    @Inject ReleaseConfiguration releaseConf
    @Inject ReleaseValidator validator

    @TaskAction
    void sendMailMessage() {

        validator.validateMailServer(releaseConf.mailServer)
        validator.validateMailPort(releaseConf.mailPort)

        validator.validateMail(releaseConf.releaseMailFrom)
        validator.validateMailList(releaseConf.releaseMailTo)

        System.properties['mail.smtp.host'] = releaseConf.mailServer
        System.properties['mail.smtp.port'] = releaseConf.mailPort

        project.configurations.mail.each {
            Project.class.classLoader.addURL(it.toURI().toURL())
        }

        def flags = releaseConf.releaseMailFlags.value
        def attachments = [
                (flags.contains('qrCode') && releaseConf.QRCodeFile?.location?.exists() ? releaseConf.QRCodeFile.location : null),
                (flags.contains('imageMontage') && releaseConf.imageMontageFile?.location?.exists() ? releaseConf.imageMontageFile.location : null)
        ]
        attachments.removeAll([null])

        ant.mail(
                mailhost: releaseConf.mailServer,
                mailport: releaseConf.mailPort,
                subject: releaseConf.releaseMailSubject,
                charset: 'UTF-8',
                tolist: releaseConf.releaseMailTo.value.join(','),
                from: releaseConf.releaseMailFrom.value,
                message: releaseConf.mailMessageFile?.location?.text,
                messageMimeType: 'text/html',
                files: attachments.join(',')
        )
    }
}
