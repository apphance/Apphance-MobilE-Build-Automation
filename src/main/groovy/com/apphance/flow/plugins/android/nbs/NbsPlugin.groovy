package com.apphance.flow.plugins.android.nbs

import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import com.apphance.flow.plugins.release.tasks.SendMail
import org.gradle.api.Plugin
import org.gradle.api.Project

class NbsPlugin implements Plugin<Project> {

    public static String IMAGE_TASK = 'image'
    public static String RELEASE_TASK = 'release'
    public static String MAIL_TASK = 'mail'
    public static String FLOW_EXTENSION = 'flow'

    @Override
    void apply(Project project) {

        project.configurations.create('mail')
        project.dependencies {
            mail 'org.apache.ant:ant-javamail:1.9.0'
            mail 'javax.mail:mail:1.4'
            mail 'javax.activation:activation:1.1.1'
        }

        project.extensions.create(FLOW_EXTENSION, FlowExtension)

        project.task(IMAGE_TASK, type: ImageMontageTask)
        project.task(RELEASE_TASK, type: AvailableArtifactsInfoTask)
        project.task(MAIL_TASK, type: SendMail, dependsOn: RELEASE_TASK)
    }
}

class FlowExtension {
    String releaseUrl
}
