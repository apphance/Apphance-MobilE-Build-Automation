package com.apphance.ameba.android

import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.*
//TODO to be removed when android plugin has its own test
class AndroidTasksSpec extends Specification {

    @Shared
    def projectSimple
    @Shared
    def projectWithoutVariants

    @Shared
    def noVariantTasks = [
            'cleanAndroid',
            'cleanClasses',
            'compileAndroid',
            'buildAll',
            'buildAllDebug',
            'buildAllRelease',
            'buildDebug-Debug',
            'buildRelease-Release',
            'installDebug',
            'installRelease',
            'replacePackage',
            'updateProject',
            'copySources',
    ]

    @Shared
    def buildTasks = [
            'cleanAndroid',
            'cleanClasses',
            'compileAndroid',
            'buildAll',
            'buildAllDebug',
            'buildAllRelease',
            'buildDebug-test',
            'buildRelease-market',
            'installDebug-test',
            'installRelease-market',
            'replacePackage',
            'updateProject',
            'copySources',
    ]

    @Shared
    def confTasks = [
            'cleanConfiguration',
            'readAndroidProjectConfiguration',
            'readAndroidVersionAndProjectName',
            'readProjectConfiguration',
    ]

    @Shared
    def setupTasks = [
            'prepareSetup',
            'verifySetup',
            'showConventions',
            'showSetup',
    ]

    @Shared
    def testTasks = [
            'checkTests',
            'testAndroid',
            'cleanAVD',
            'createAVD',
            'startEmulator',
            'stopAllEmulators',
            'prepareRobotium',
            'prepareRobolectric',
            'testRobolectric',
    ]

    def setupSpec() {

        ProjectBuilder projectBuilder = ProjectBuilder.builder()

        projectBuilder.withProjectDir(new File('testProjects/android/android-basic'))
        projectSimple = projectBuilder.build()
        projectSimple.project.plugins.apply(AmebaPlugin)

        projectBuilder.withProjectDir(new File('testProjects/android/android-novariants'))
        projectWithoutVariants = projectBuilder.build()
        projectWithoutVariants.project.plugins.apply(AmebaPlugin)
    }

    def 'plugins task are in correct group'() {

        when:
        def groups = tasks.collect { project.tasks[it].group }

        then:
        tasksGroups == groups

        where:
        project                | tasks          | tasksGroups
        projectWithoutVariants | noVariantTasks | [AMEBA_BUILD] * noVariantTasks.size()
        projectSimple          | buildTasks     | [AMEBA_BUILD] * buildTasks.size()
        projectSimple          | confTasks      | [AMEBA_CONFIGURATION] * confTasks.size()
        projectSimple          | setupTasks     | [AMEBA_SETUP] * setupTasks.size()
        projectSimple          | []             | [AMEBA_RELEASE] * 0
        projectSimple          | testTasks      | [AMEBA_TEST] * testTasks.size()

    }
}
