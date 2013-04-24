package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import org.apache.tools.ant.Project
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.util.regex.Pattern

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class SendMailMessageTask extends DefaultTask {

    static String NAME = 'sendMailMessage'
    String group = AMEBA_RELEASE
    String description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode,imageMontage"""

    private Pattern WHITESPACE = Pattern.compile('\\s+')

    @Inject
    private ReleaseConfiguration releaseConf

    @TaskAction
    void sendMailMessage() {

        validateMailServer(releaseConf.mailServer)
        validateMailPort(releaseConf.mailPort)

        validate(releaseConf.releaseMailFrom)
        validate(releaseConf.releaseMailTo)

        Properties props = System.getProperties()
        props.put('mail.smtp.host', releaseConf.mailServer)
        props.put('mail.smtp.port', releaseConf.mailPort)

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

    @groovy.transform.PackageScope
    void validateMailServer(String mailServer) {
        mailServer = mailServer?.trim()
        if (!mailServer || mailServer?.empty || WHITESPACE.matcher(mailServer ?: '').find())
            throw new GradleException("""|Property 'mail.server' has invalid value!
                                         |Set it either by 'mail.server' system property or
                                         |'MAIL_SERVER' environment variable!""".stripMargin())
    }

    @groovy.transform.PackageScope
    void validateMailPort(String mailPort) {
        mailPort = mailPort?.trim()
        if (mailPort?.empty || !mailPort?.matches('[0-9]+')) {
            throw new GradleException("""|Property 'mail.port' has invalid value!
                                         |Set it either by 'mail.port' system property or 'MAIL_PORT' environment variable.
                                         |This property must have numeric value!""".stripMargin())
        }
    }

    @groovy.transform.PackageScope
    void validate(StringProperty mail) {
        if (!mail.validator(mail.value)) {
            throw new GradleException("""|Property ${mail.name} is not set!
                                         |Shoud be valid email address!""".stripMargin())
        }
    }
}
