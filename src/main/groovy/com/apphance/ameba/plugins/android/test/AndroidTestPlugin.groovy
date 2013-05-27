package com.apphance.ameba.plugins.android.test

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import com.apphance.ameba.plugins.android.buildplugin.tasks.CompileAndroidTask
import com.apphance.ameba.plugins.android.test.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Performs android testing.
 */
class AndroidTestPlugin implements Plugin<Project> {

    @Inject AndroidTestConfiguration testConf
    @Inject AndroidConfiguration conf

    @Override
    void apply(Project project) {
        if (testConf.isEnabled()) {

            if (testConf.emmaEnabled.value) {
                project.configurations.add('emma')
                project.dependencies.add('emma', project.files([
                        new File(conf.SDKDir, 'tools/lib/emma.jar')
                ]))
                project.dependencies.add('emma', project.files([
                        new File(conf.SDKDir, 'tools/lib/emma_ant.jar')
                ]))
            }

            project.task(CreateAVDTask.NAME,
                    type: CreateAVDTask
            )

            project.task(CleanAVDTask.NAME,
                    type: CleanAVDTask
            )

            project.task(TestAndroidTask.NAME,
                    type: TestAndroidTask,
                    dependsOn: CreateAVDTask.NAME)

            project.task(StopAllEmulatorsTask.NAME,
                    type: StopAllEmulatorsTask)

            project.task(StartEmulatorTask.NAME,
                    type: StartEmulatorTask)

            project.task(TestRobolectricTask.NAME,
                    type: TestRobolectricTask,
                    dependsOn: [CompileAndroidTask.NAME])

            project.configurations.add('robotium')
            project.dependencies.add('robotium', 'com.jayway.android.robotium:robotium-solo:3.1')
            project.task(PrepareRobotiumTask.NAME,
                    type: PrepareRobotiumTask)

            project.configurations.add('robolectric')
            project.dependencies.add('robolectric', 'com.pivotallabs:robolectric:1.1')
            project.dependencies.add('robolectric', 'junit:junit:4.10')
            project.task(PrepareRobolectricTask.NAME,
                    type: PrepareRobolectricTask)

        }
    }
}