package com.apphance.ameba.plugins.ios.release

import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.READ_IOS_PROJECT_VERSIONS_TASK_NAME
import static com.apphance.ameba.plugins.ios.release.IOSReleasePlugin.*
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.SEND_MAIL_MESSAGE_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSReleasePluginSpec extends Specification {


    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        and: 'add fake task send mail task to satisfy dependencies'
        project.task(SEND_MAIL_MESSAGE_TASK_NAME)

        when:
        project.plugins.apply(IOSReleasePlugin)

        then: 'every single task is in correct group'
        project.tasks[UPDATE_VERSION_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[BUILD_DOCUMENTATION_ZIP_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME].group == AMEBA_RELEASE
        project.tasks[PREPARE_MAIL_MESSAGE_TASK_NAME].group == AMEBA_RELEASE

        and: 'task dependencies configured correctly'
        project.tasks[UPDATE_VERSION_TASK_NAME].dependsOn.contains(READ_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME].dependsOn.containsAll(
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                READ_IOS_PROJECT_VERSIONS_TASK_NAME)
        project.tasks[PREPARE_MAIL_MESSAGE_TASK_NAME].dependsOn.containsAll(
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME)
    }
}
