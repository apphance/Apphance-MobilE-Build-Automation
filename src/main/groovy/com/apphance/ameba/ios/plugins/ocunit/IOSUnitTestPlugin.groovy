package com.apphance.ameba.ios.plugins.ocunit

import groovy.lang.Closure

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin

class IOSUnitTestPlugin implements Plugin<Project> {

    static final String AMEBA_IOS_UNIT = 'Ameba iOS OCUnit tests'

    Logger logger = Logging.getLogger(IOSUnitTestPlugin.class)
    Project project
    ProjectConfiguration conf
    ProjectHelper projectHelper
    IOSProjectConfiguration iosConf

    void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use (PropertyCategory) {
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
        task.description = "Build and executes Unit tests. Requires UnitTests target. Produces result in tmp directory ${project.iosUnitTests.target} ${project.iosUnitTests.configuration}"
        task.group = AMEBA_IOS_UNIT
        task << {
            def configuration = project.iosUnitTests.configuration
            def target = project.iosUnitTests.target
            logger.lifecycle( "\n\n\n=== Building DEBUG target ${project.iosUnitTests.target}, configuration ${project.iosUnitTests.configuration}  ===")
            def result = projectHelper.executeCommand(project, [
                "xcodebuild" ,
                "-target",
                project.iosUnitTests.target,
                "-configuration",
                project.iosUnitTests.configuration,
                "-sdk",
                iosConf.simulatorsdk
            ], failOnError = false)
            OCUnitParser parser = new OCUnitParser()
            parser.parse(result)
            File unitTestFile = new File (conf.tmpDirectory, "TEST-all.xml")
            conf.tmpDirectory.mkdirs()
            new XMLJunitExporter(unitTestFile,parser.testSuites).export()
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
