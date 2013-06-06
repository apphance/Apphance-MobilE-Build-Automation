package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.release.ReleaseConfiguration
import org.apache.tools.ant.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.configuration.release.ReleaseConfiguration.*
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class SendMailMessageTask extends DefaultTask {

    static String NAME = 'sendMailMessage'
    String group = AMEBA_RELEASE
    String description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode, imageMontage, installableSimulator"""



    @Inject ReleaseConfiguration releaseConf
    @Inject org.gradle.api.AntBuilder ant

    @TaskAction
    void sendMailMessage() {

        validateMailServer(releaseConf.mailServer)
        validateMailPort(releaseConf.mailPort)

        validateMail(releaseConf.releaseMailFrom)
        validateMail(releaseConf.releaseMailTo)

        Properties props = System.getProperties()
        props.put('mail.smtp.host', releaseConf.mailServer)
        props.put('mail.smtp.port', releaseConf.mailPort)

        project.configurations.mail.each {
            Project.class.classLoader.addURL(it.toURI().toURL())
        }

        ant.mail(
                mailhost: releaseConf.mailServer,
                mailport: releaseConf.mailPort,
                subject: releaseConf.releaseMailSubject,
                charset: 'utf-8',
                tolist: releaseConf.releaseMailTo.value) {
            from(address: releaseConf.releaseMailFrom.value)
            message(mimetype: "text/html", releaseConf.mailMessageFile?.location?.text)
            if (releaseConf.releaseMailFlags.value.contains("qrCode")) {
                fileset(file: releaseConf.QRCodeFile.location)
            }
            if (releaseConf.releaseMailFlags.value.contains("imageMontage") && releaseConf.imageMontageFile != null) {
                fileset(file: releaseConf.imageMontageFile.location)
            }
        }
    }
}
