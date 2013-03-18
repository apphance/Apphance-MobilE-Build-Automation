package com.apphance.ameba.android.plugins.release

import spock.lang.Specification

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.android.plugins.release.AndroidReleasePlugin.*
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
        project.tasks[UPDATE_VERSION_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[BUILD_DOCUMENTATION_ZIP_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[PREPARE_MAIL_MESSAGE_TASK_NAME].group == AMEBA_RELEASE

        then: 'every task has correct dependencies'
        project.tasks[UPDATE_VERSION_TASK_NAME].dependsOn.contains('readAndroidProjectConfiguration')

        project.tasks[BUILD_DOCUMENTATION_ZIP_TASK_NAME].dependsOn.containsAll(JAVADOC_TASK_NAME,
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)

        project.tasks[PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME].dependsOn.contains('readAndroidProjectConfiguration')

        project.tasks[PREPARE_MAIL_MESSAGE_TASK_NAME].dependsOn.containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)

        then: 'sendMailMessage tasks depends on prepareMailMessage'
        project.tasks[SEND_MAIL_MESSAGE_TASK_NAME].dependsOn.contains(PREPARE_MAIL_MESSAGE_TASK_NAME)
    }
}
