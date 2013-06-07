package com.apphance.flow.plugins.android.jarlibrary

import com.apphance.flow.configuration.android.AndroidJarLibraryConfiguration
import com.apphance.flow.plugins.android.jarlibrary.tasks.DeployJarLibraryTask
import com.apphance.flow.plugins.android.jarlibrary.tasks.JarLibraryTask
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidJarLibraryPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {

        given:
        def project = builder().build()

        and:
        def ajlp = new AndroidJarLibraryPlugin()

        and: 'create mock android release configuration and set it'
        def ajlc = Mock(AndroidJarLibraryConfiguration)
        ajlc.isEnabled() >> true
        ajlp.jarLibConf = ajlc

        when:
        ajlp.apply(project)

        then: 'configuration is added'
        project.configurations.jarLibraryConfiguration

        then: 'every single task is in correct group'
        project.tasks[JarLibraryTask.NAME].group == FLOW_BUILD.name()
        project.tasks[DeployJarLibraryTask.NAME].group == FLOW_BUILD.name()

        then: 'every task has correct dependencies'
        project.tasks[DeployJarLibraryTask.NAME].dependsOn.flatten().contains(JarLibraryTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def ajlp = new AndroidJarLibraryPlugin()

        and: 'create mock android release configuration and set it'
        def ajlc = Mock(AndroidJarLibraryConfiguration)
        ajlc.isEnabled() >> false
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