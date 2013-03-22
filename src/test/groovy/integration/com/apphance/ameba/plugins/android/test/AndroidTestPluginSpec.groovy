package com.apphance.ameba.plugins.android.test

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.android.test.AndroidTestPlugin.*
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidTestPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        and:
        project.plugins.apply(ProjectConfigurationPlugin)

        when:
        project.plugins.apply(AndroidTestPlugin)

        then: 'android test convention is added'
        project.convention.plugins['androidTest']

        then: 'every single task is in correct group'
        project.tasks[READ_ANDROID_TEST_CONFIGURATION_TASK_NAME].group == AMEBA_CONFIGURATION
        project.tasks[CREATE_AVD_TASK_NAME].group == AMEBA_TEST
        project.tasks[CLEAN_AVD_TASK_NAME].group == AMEBA_TEST
        project.tasks[TEST_ANDROID_TASK_NAME].group == AMEBA_TEST
        project.tasks[STOP_ALL_EMULATORS_TASK_NAME].group == AMEBA_TEST
        project.tasks[START_EMULATOR_TASK_NAME].group == AMEBA_TEST
        project.tasks[TEST_ROBOLECTRIC_TASK_NAME].group == AMEBA_TEST
        project.tasks[PREPARE_ROBOTIUM_TASK_NAME].group == AMEBA_TEST
        project.tasks[PREPARE_ROBOLECTRIC_TASK_NAME].group == AMEBA_TEST

        then: 'every task has correct dependencies'
        project.tasks[READ_ANDROID_TEST_CONFIGURATION_TASK_NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[CREATE_AVD_TASK_NAME].dependsOn.contains(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
        project.tasks[CLEAN_AVD_TASK_NAME].dependsOn.contains(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
        project.tasks[TEST_ANDROID_TASK_NAME].dependsOn.contains(CREATE_AVD_TASK_NAME)
        project.tasks[START_EMULATOR_TASK_NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[TEST_ROBOLECTRIC_TASK_NAME].dependsOn.contains(COMPILE_JAVA_TASK_NAME)
        project.tasks[PREPARE_ROBOTIUM_TASK_NAME].dependsOn.contains(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
        project.tasks[PREPARE_ROBOLECTRIC_TASK_NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

        then: 'robotium configuration and dependencies added'
        project.configurations.robotium
        project.dependencies.configurationContainer.robotium.allDependencies.size() == 1

        then: 'robolectric configuration and dependencies added'
        project.configurations.robolectric
        project.dependencies.configurationContainer.robolectric.allDependencies.size() == 2
    }
}
