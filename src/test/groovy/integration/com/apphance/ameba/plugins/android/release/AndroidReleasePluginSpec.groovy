package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.android.release.tasks.BuildDocZipTask
import com.apphance.ameba.plugins.android.release.tasks.MailMessageTask
import com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_FOR_RELEASE_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.SEND_MAIL_MESSAGE_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.JAVADOC_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidReleasePluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        and: 'add fake task, otherwise ProjectReleasePlugin must be loaded'
        project.task(SEND_MAIL_MESSAGE_TASK_NAME)

        when:
        project.plugins.apply(AndroidReleasePlugin)

        then: 'every single task is in correct group'
        project.tasks[UpdateVersionTask.name].group == AMEBA_RELEASE
        project.tasks[BuildDocZipTask.name].group == AMEBA_RELEASE
        project.tasks[AvailableArtifactsInfoTask.name].group == AMEBA_RELEASE
        project.tasks[MailMessageTask.name].group == AMEBA_RELEASE

        then: 'every task has correct dependencies'
        project.tasks[UpdateVersionTask.name].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

        project.tasks[BuildDocZipTask.name].dependsOn.flatten().containsAll(JAVADOC_TASK_NAME,
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)

        project.tasks[AvailableArtifactsInfoTask.name].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

        project.tasks[MailMessageTask.name].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                AvailableArtifactsInfoTask.name,
                PREPARE_FOR_RELEASE_TASK_NAME)

        then: 'sendMailMessage tasks depends on prepareMailMessage'
        project.tasks[SEND_MAIL_MESSAGE_TASK_NAME].dependsOn.contains(MailMessageTask.name)
    }
}
