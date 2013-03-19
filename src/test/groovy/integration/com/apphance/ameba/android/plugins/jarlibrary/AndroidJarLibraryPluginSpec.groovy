package com.apphance.ameba.android.plugins.jarlibrary

import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import spock.lang.Specification

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryPlugin.DEPLOY_JAR_LIBRARY_TASK_NAME
import static com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryPlugin.JAR_LIBRARY_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidJarLibraryPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(ProjectConfigurationPlugin)
        project.plugins.apply(AndroidJarLibraryPlugin)

        then: 'each task has correct group'
        project.tasks[JAR_LIBRARY_TASK_NAME].group == AMEBA_BUILD
        project.tasks[DEPLOY_JAR_LIBRARY_TASK_NAME].group == AMEBA_BUILD

        then: 'each task has correct dependencies'
        project.tasks[JAR_LIBRARY_TASK_NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
        project.tasks[DEPLOY_JAR_LIBRARY_TASK_NAME].dependsOn.contains(JAR_LIBRARY_TASK_NAME)

        then: "$DEPLOY_JAR_LIBRARY_TASK_NAME task has added configuration"
        project.configurations.jarLibraryConfiguration
    }
}