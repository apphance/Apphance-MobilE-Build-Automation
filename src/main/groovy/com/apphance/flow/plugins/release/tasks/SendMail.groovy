package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask
import org.apache.tools.ant.Project
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.flow.plugins.android.nbs.NbsPlugin.IMAGE_TASK
import static com.apphance.flow.plugins.android.nbs.NbsPlugin.MAIL_TASK
import static com.apphance.flow.plugins.android.nbs.NbsPlugin.RELEASE_TASK
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE

/**
 * Send mail task for Android New Build System.
 */
class SendMail extends DefaultTask {

    String group = FLOW_RELEASE
    String description = 'Send mail with release info'

    String to
    String from
    String subject
    String mailhost = 'localhost'
    String mailport = '25'
    String smtpUser
    String smtpPassword
    String ssl = 'false'

    @TaskAction
    void sendMailMessage() {
        if (!to) throw new GradleException("Configure email recipient! Property 'to' in $MAIL_TASK task.")
        if (!from) throw new GradleException("Configure email sender! Property 'from' in $MAIL_TASK task.")

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
                mailport: mailport,
                user: smtpUser,
                password: smtpPassword,
                ssl: ssl,
                messageMimeType: 'text/html',
                files: attachments*.absolutePath.join(',')
        )
    }
}
