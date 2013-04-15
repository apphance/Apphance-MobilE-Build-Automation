package com.apphance.ameba.plugins.android.jarlibrary

import com.apphance.ameba.configuration.android.AndroidJarLibraryConfiguration
import com.apphance.ameba.plugins.android.jarlibrary.tasks.DeployJarLibraryTask
import com.apphance.ameba.plugins.android.jarlibrary.tasks.JarLibraryTask
import com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.android.jarlibrary.AndroidJarLibraryPlugin.DEPLOY_JAR_LIBRARY_TASK_NAME
import static com.apphance.ameba.plugins.android.jarlibrary.AndroidJarLibraryPlugin.JAR_LIBRARY_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidJarLibraryPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {

        given:
        def project = builder().build()

        and:
        def ajlp = new AndroidJarLibraryPlugin()

        and: 'create mock android release configuration and set it'
        def ajlc = Spy(AndroidJarLibraryConfiguration)
        ajlc.isActive() >> true
        ajlp.jarLibConf = ajlc

        when:
        ajlp.apply(project)

        then: 'configuration is added'
        project.configurations.jarLibraryConfiguration

        then: 'every single task is in correct group'
        project.tasks[JarLibraryTask.NAME].group == AMEBA_BUILD
        project.tasks[DeployJarLibraryTask.NAME].group == AMEBA_BUILD

        then: 'every task has correct dependencies'
        project.tasks[JarLibraryTask.NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

        project.tasks[DeployJarLibraryTask.NAME].dependsOn.flatten().contains(JarLibraryTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def ajlp = new AndroidJarLibraryPlugin()

        and: 'create mock android release configuration and set it'
        def ajlc = Spy(AndroidJarLibraryConfiguration)
        ajlc.isActive() >> false
        ajlp.jarLibConf = ajlc

        when:
        ajlp.apply(project)

        then:
        !project.configurations.findByName('jarLibraryConfiguration')

        then:
        !project.getTasksByName(JarLibraryTask.NAME, false)
        !project.getTasksByName(DeployJarLibraryTask.NAME, false)
    }
}