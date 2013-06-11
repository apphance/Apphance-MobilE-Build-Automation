package com.apphance.flow.plugins.android.test

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.plugins.android.buildplugin.tasks.CompileAndroidTask
import com.apphance.flow.plugins.android.test.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

/**
 * Performs android testing.
 */
class AndroidTestPlugin implements Plugin<Project> {

    def log = getLogger(this.class)

    @Inject AndroidTestConfiguration testConf
    @Inject AndroidConfiguration conf

    @Override
    void apply(Project project) {
        if (testConf.isEnabled()) {
            log.lifecycle("Applying plugin ${this.class.simpleName}")

            if (testConf.emmaEnabled.value) {
                project.configurations.create('emma')
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

            project.configurations.create('robolectric')
            project.dependencies.add('robolectric', 'com.pivotallabs:robolectric:1.1')
            project.dependencies.add('robolectric', 'junit:junit:4.10')
            project.task(PrepareRobolectricTask.NAME,
                    type: PrepareRobolectricTask,
                    dependsOn: CompileAndroidTask.NAME)

            project.task(TestRobolectricTask.NAME,
                    type: TestRobolectricTask,
                    dependsOn: [PrepareRobolectricTask.NAME])

            project.configurations.create('robotium')
            project.dependencies.add('robotium', 'com.jayway.android.robotium:robotium-solo:3.1')
            project.task(PrepareRobotiumTask.NAME,
                    type: PrepareRobotiumTask)


        }
    }
}