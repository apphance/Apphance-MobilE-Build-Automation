package com.apphance.ameba.ios.plugins.ocunit

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Unit test plugin - all unit tests are run here.
 *
 */
class IOSUnitTestPlugin implements Plugin<Project> {

    static final String AMEBA_IOS_UNIT = 'Ameba iOS OCUnit tests'

    Logger logger = Logging.getLogger(IOSUnitTestPlugin.class)
    Project project
    ProjectConfiguration conf
    ProjectHelper projectHelper
    IOSProjectConfiguration iosConf

    void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use(PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
            project.extensions.iosUnitTests = new IOSUnitTestConvention()
            prepareRunUnitTestsTask()
        }
    }

    private void prepareRunUnitTestsTask() {
        def task = project.task('runUnitTests')
        task.description = "Build and executes Unit tests. Requires UnitTests target."
        task.group = AMEBA_IOS_UNIT
        task << {
            def configuration = project.iosUnitTests.configuration
            def target = project.iosUnitTests.target
            logger.lifecycle("\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
            def result = projectHelper.executeCommand(project, iosConf.getXCodeBuildExecutionPath(target, configuration) + [
                    "-target",
                    target,
                    "-configuration",
                    configuration,
                    "-sdk",
                    iosConf.simulatorsdk
            ], failOnError = false)
            OCUnitParser parser = new OCUnitParser()
            parser.parse(result)
            File unitTestFile = new File(conf.tmpDirectory, "TEST-all.xml")
            conf.tmpDirectory.mkdirs()
            new XMLJunitExporter(unitTestFile, parser.testSuites).export()
        }
        task.dependsOn(project.readProjectConfiguration)
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
        """This plugins provides functionality of standard ocunit testing for iOS.

It executes all tests which are build using ocunit test framework.

More description needed ....

"""
}
