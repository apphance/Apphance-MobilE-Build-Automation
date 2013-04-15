package com.apphance.ameba.plugins.android.test

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import com.apphance.ameba.configuration.properties.BooleanProperty
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.plugins.android.test.tasks.*
import com.google.common.io.Files
import spock.lang.Specification

import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidTestPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def atp = new AndroidTestPlugin()

        and:
        def atc = GroovyStub(AndroidTestConfiguration)
        atc.isActive() >> true
        atc.emmaEnabled >> new BooleanProperty(value: true)
        atp.testConf = atc

        and:
        def ac = GroovyStub(AndroidConfiguration)
        ac.sdkDir >> new FileProperty(value: Files.createTempDir())
        atp.androidConf = ac

        when:
        atp.apply(project)

        then: 'every single task is in correct group'
        project.tasks[CreateAVDTask.NAME]
        project.tasks[CleanAVDTask.NAME]
        project.tasks[TestAndroidTask.NAME]
        project.tasks[StopAllEmulatorsTask.NAME]
        project.tasks[StartEmulatorTask.NAME]
        project.tasks[TestRobolectricTask.NAME]
        project.tasks[PrepareRobotiumTask.NAME]
        project.tasks[PrepareRobolectricTask.NAME]

        then: 'every task has correct dependencies'
        project.tasks[TestAndroidTask.NAME].dependsOn.flatten().contains(CreateAVDTask.NAME)
        project.tasks[TestRobolectricTask.NAME].dependsOn.flatten().contains(COMPILE_JAVA_TASK_NAME)

        then: 'robotium configuration and dependencies added'
        project.configurations.robotium
        project.dependencies.configurationContainer.robotium.allDependencies.size() == 1

        then: 'robolectric configuration and dependencies added'
        project.configurations.robolectric
        project.dependencies.configurationContainer.robolectric.allDependencies.size() == 2
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def atp = new AndroidTestPlugin()

        and:
        def atc = Mock(AndroidTestConfiguration)
        atc.isActive() >> false
        atp.testConf = atc

        when:
        atp.apply(project)

        then:
        !project.tasks.findByName(CreateAVDTask.NAME)
        !project.tasks.findByName(CleanAVDTask.NAME)
        !project.tasks.findByName(TestAndroidTask.NAME)
        !project.tasks.findByName(StopAllEmulatorsTask.NAME)
        !project.tasks.findByName(StartEmulatorTask.NAME)
        !project.tasks.findByName(TestRobolectricTask.NAME)
        !project.tasks.findByName(PrepareRobotiumTask.NAME)
        !project.tasks.findByName(PrepareRobolectricTask.NAME)

        then:
        !project.configurations.findByName('robotium')
        !project.configurations.findByName('robolectric')
    }
}
