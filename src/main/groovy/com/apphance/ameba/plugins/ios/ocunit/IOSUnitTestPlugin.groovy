package com.apphance.ameba.plugins.ios.ocunit

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.ocunit.tasks.RunUnitTestsTasks
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static org.gradle.api.logging.Logging.getLogger

/**
 * Unit test plugin - all unit tests are run here.
 *
 */
class IOSUnitTestPlugin implements Plugin<Project> {

    public static final String AMEBA_IOS_UNIT = 'Ameba iOS OCUnit tests'
    public static final String RUN_UNIT_TESTS_TASK_NAME = 'runUnitTests'

    def l = getLogger(getClass())

    @Inject
    private IOSExecutor iosExecutor

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        addIOSUnitTestsConvention()
        prepareRunUnitTestsTask()
    }

    private void addIOSUnitTestsConvention() {
        project.convention.plugins.put('iosUnitTests', new IOSUnitTestConvention())
    }

    private void prepareRunUnitTestsTask() {
        def task = project.task(RUN_UNIT_TESTS_TASK_NAME)
        task.description = "Build and executes Unit tests. Requires UnitTests target."
        task.group = AMEBA_IOS_UNIT
        task << { new RunUnitTestsTasks(project, iosExecutor).runUnitTests() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    class IOSUnitTestConvention {
        def String target = 'UnitTests'
        def String configuration = 'Release'

        def iosUnitTests(Closure close) {
            close.delegate = this
            close.run()
        }
    }

    static public final String DESCRIPTION =
        """|This plugins provides functionality of standard ocunit testing for iOS.
           |
           |It executes all tests which are build using ocunit test framework.
           |
           |More description needed ....
           |""".stripMargin()
}
