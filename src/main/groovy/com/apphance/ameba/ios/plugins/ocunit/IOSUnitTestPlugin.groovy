package com.apphance.ameba.ios.plugins.ocunit

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

/**
 * Unit test plugin - all unit tests are run here.
 *
 */
class IOSUnitTestPlugin implements Plugin<Project> {

    static final String AMEBA_IOS_UNIT = 'Ameba iOS OCUnit tests'

    def l = getLogger(getClass())

    @Inject
    CommandExecutor executor

    @Inject
    IOSExecutor iosExecutor

    Project project
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf

    @Override
    void apply(Project project) {
        use(PropertyCategory) {
            this.project = project
            this.conf = project.getProjectConfiguration()
            this.iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
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
            l.lifecycle("\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
            conf.tmpDirectory.mkdirs()
            def testResults = new File(conf.tmpDirectory, "test-${target}-${configuration}.txt")
            l.lifecycle("Trying to create file: ${testResults.canonicalPath}")
            testResults.createNewFile()
            iosExecutor.buildTestTarget(project.rootDir, target, configuration, "${testResults.canonicalPath}".toString())
            OCUnitParser parser = new OCUnitParser()
            parser.parse(testResults.text.split('\n') as List)
            File unitTestFile = new File(conf.tmpDirectory, "TEST-all.xml")
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
        """|This plugins provides functionality of standard ocunit testing for iOS.
           |
           |It executes all tests which are build using ocunit test framework.
           |
           |More description needed ....
           |""".stripMargin()
}
