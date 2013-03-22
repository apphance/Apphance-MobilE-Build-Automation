package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.readPropertyOrEnvironmentVariable
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData

class SendMailMessageTask {

    private Project project
    private AntBuilder ant
    private ProjectReleaseConfiguration releaseConf

    SendMailMessageTask(Project project) {
        this.project = project
        this.ant = project.ant
        this.releaseConf = retrieveProjectReleaseData(project)
    }

    void sendMailMessage() {
        def mailServer = readPropertyOrEnvironmentVariable(project, 'mail.server')
        def mailPort = readPropertyOrEnvironmentVariable(project, 'mail.port')

        Properties props = System.getProperties()
        props.put('mail.smtp.host', mailServer)
        props.put('mail.smtp.port', mailPort)

        project.configurations.mail.each {
            org.apache.tools.ant.Project.class.classLoader.addURL(it.toURI().toURL())
        }

        ant.mail(
                mailhost: mailServer,
                mailport: mailPort,
                subject: releaseConf.releaseMailSubject,
                charset: 'utf-8',
                tolist: releaseConf.releaseMailTo) {
            from(address: releaseConf.releaseMailFrom)
            message(mimetype: "text/html", releaseConf.mailMessageFile.location.text)
            if (releaseConf.releaseMailFlags.contains("qrCode")) {
                fileset(file: releaseConf.qrCodeFile.location)
            }
            if (releaseConf.releaseMailFlags.contains("imageMontage") && releaseConf.imageMontageFile != null) {
                fileset(file: releaseConf.imageMontageFile.location)
            }
        }
    }
}
