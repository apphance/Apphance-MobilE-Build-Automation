package com.apphance.ameba.plugins.ios.ocunit.tasks

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

class RunUnitTestsTasks {

    private l = getLogger(getClass())

    private Project project
    private IOSExecutor iosExecutor
    private ProjectConfiguration conf

    RunUnitTestsTasks(Project project, IOSExecutor iosExecutor) {
        this.project = project
        this.iosExecutor = iosExecutor
        this.conf = PropertyCategory.getProjectConfiguration(project)
    }

    void runUnitTests() {
        def configuration = project.iosUnitTests.configuration
        def target = project.iosUnitTests.target
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
}
