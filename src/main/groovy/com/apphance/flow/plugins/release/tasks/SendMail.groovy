package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask
import org.apache.tools.ant.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.flow.plugins.android.nbs.NbsPlugin.IMAGE_TASK
import static com.apphance.flow.plugins.android.nbs.NbsPlugin.RELEASE_TASK

class SendMail extends DefaultTask {

    String to
    String from
    String subject
    String mailhost = 'localhost'

    @TaskAction
    void sendMailMessage() {
        def release = project.tasks.findByName(RELEASE_TASK) as AvailableArtifactsInfoTask
        def image = project.tasks.findByName(IMAGE_TASK) as ImageMontageTask

        subject = "Release $project.name version: ${release.versionString}"

        project.configurations.mail.each {
            Project.class.classLoader.addURL(it.toURI().toURL())
        }

        List<File> attachments = [image?.imageMontageArtifact?.location, release?.QRCodeFile?.location].findAll { it && it.exists() }

        logger.info "Sending mail to: $to, from: $from, subject: $subject, attachments: ${attachments*.absolutePath}"

        ant.mail(
                subject: subject,
                charset: 'UTF-8',
                tolist: to,
                from: from,
                message: release.mailMessageFile?.location?.text,
                mailhost: mailhost,
                messageMimeType: 'text/html',
                files: attachments*.absolutePath.join(',')
        )
    }
}
