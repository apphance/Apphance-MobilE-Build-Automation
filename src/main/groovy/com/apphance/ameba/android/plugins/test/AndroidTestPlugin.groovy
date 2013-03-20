package com.apphance.ameba.android.plugins.test

import com.apphance.ameba.android.plugins.test.tasks.*
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME

/**
 * Performs android testing.
 */
class AndroidTestPlugin implements Plugin<Project> {

    @Inject
    private CommandExecutor executor

    @Inject
    AndroidExecutor androidExecutor

    private Project project

    public static final String READ_ANDROID_TEST_CONFIGURATION_TASK_NAME = 'readAndroidTestConfiguration'
    public static final String CREATE_AVD_TASK_NAME = 'createAVD'
    public static final String CLEAN_AVD_TASK_NAME = 'cleanAVD'
    public static final String TEST_ANDROID_TASK_NAME = 'testAndroid'
    public static final String STOP_ALL_EMULATORS_TASK_NAME = 'stopAllEmulators'
    public static final String START_EMULATOR_TASK_NAME = 'startEmulator'
    public static final String TEST_ROBOLECTRIC_TASK_NAME = 'testRobolectric'
    public static final String PREPARE_ROBOTIUM_TASK_NAME = 'prepareRobotium'
    public static final String PREPARE_ROBOLECTRIC_TASK_NAME = 'prepareRobolectric'

    @Override
    void apply(Project project) {
        this.project = project

        addAndroidTestConvention()
        prepareReadAndroidTestConfigurationTask()
        prepareCreateAvdTask()
        prepareCleanAvdTask()
        prepareAndroidTestingTask()
        prepareStopAllEmulatorsTask()
        prepareStartEmulatorTask()
        prepareAndroidRobolectricTask()
        prepareAndroidRobotiumStructure()
        prepareAndroidRobolectricStructure()

        project.prepareSetup.prepareSetupOperations << new PrepareAndroidTestSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyAndroidTestSetupOperation()
        project.showSetup.showSetupOperations << new ShowAndroidTestSetupOperation()
    }

    private void addAndroidTestConvention() {
        project.convention.plugins.put('androidTest', new AndroidTestConvention())
    }

    private void prepareReadAndroidTestConfigurationTask() {
        def task = project.task(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
        task.description = 'Reads android test configuration'
        task.group = AMEBA_CONFIGURATION
        task << { new ReadAndroidTestConfigurationTask(project).readAndroidConfiguration() }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareCreateAvdTask() {
        def task = project.task(CREATE_AVD_TASK_NAME)
        task.description = 'Prepares AVDs for emulator'
        task.group = AMEBA_TEST
        task << { new CreateAVDTask(project, androidExecutor).createAVD() }
        task.dependsOn(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
    }

    private void prepareCleanAvdTask() {
        def task = project.task(CLEAN_AVD_TASK_NAME)
        task.description = 'Cleans AVDs for emulators'
        task.group = AMEBA_TEST
        task << { new CleanAVDTask(project).cleanAVD() }
        task.dependsOn(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
    }

    private void prepareAndroidTestingTask() {
        def task = project.task(TEST_ANDROID_TASK_NAME)
        task.description = 'Runs android tests on the project'
        task.group = AMEBA_TEST
        task << { new TestAndroidTask(project, executor).testAndroid() }
        task.dependsOn(CREATE_AVD_TASK_NAME)
    }

    private void prepareStopAllEmulatorsTask() {
        def task = project.task(STOP_ALL_EMULATORS_TASK_NAME)
        task.description = 'Stops all emulators and accompanying logcat (includes stopping adb)'
        task.group = AMEBA_TEST
        task << { new StopAllEmulatorsTask(project, executor).stopAllEmulators() }
    }

    private void prepareStartEmulatorTask() {
        def task = project.task(START_EMULATOR_TASK_NAME)
        task.description = 'Starts emulator for manual inspection'
        task.group = AMEBA_TEST
        task << { new StartEmulatorTask(project, executor).startEmulator() }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareAndroidRobolectricTask() {
        def task = project.task(TEST_ROBOLECTRIC_TASK_NAME)
        task.description = 'Runs Robolectric test on the project'
        task.group = AMEBA_TEST
        task << { new TestRobolectricTask(project).testRobolectric() }
        task.dependsOn(COMPILE_JAVA_TASK_NAME)
    }

    private void prepareAndroidRobotiumStructure() {
        def task = project.task(PREPARE_ROBOTIUM_TASK_NAME)
        task.description = 'Prepares file structure for Robotium test framework'
        task.group = AMEBA_TEST
        project.configurations.add('robotium')
        project.dependencies.add('robotium', 'com.jayway.android.robotium:robotium-solo:3.1')
        task << { new PrepareRobotiumTask(project, executor).prepareRobotium() }
        task.dependsOn(READ_ANDROID_TEST_CONFIGURATION_TASK_NAME)
    }

    private void prepareAndroidRobolectricStructure() {
        def task = project.task(PREPARE_ROBOLECTRIC_TASK_NAME)
        task.description = 'Prepares file structure for Robolectric test framework'
        task.group = AMEBA_TEST
        project.configurations.add('robolectric')
        project.dependencies.add('robolectric', 'com.pivotallabs:robolectric:1.1')
        project.dependencies.add('robolectric', 'junit:junit:4.10')
        task << { new PrepareRobolectricTask(project).prepareRobolectric() }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    static class AndroidTestConvention {
        static public final String DESCRIPTION =
            """The convention provides port address range which is used by android emulator.
It also defines maximum time (in ms) to start android emulator and retry time (in ms.) between trying to
reconnect to the emulator.
"""
        def int startPort = 5554
        def int endPort = 5584
        def int maxEmulatorStartupTime = 360 * 1000
        def int retryTime = 4 * 1000
        def String robotiumPath = '/test/android'
        def String robolectricPath = '/test/robolectric'

        def androidTest(Closure close) {
            close.delegate = this
            close.run()
        }
    }

    static public final String DESCRIPTION =
        """This plugin provides easy automated testing framework for Android applications

It has support for two level of tests: integration testing done usually with the
help of robolectric."""

}
