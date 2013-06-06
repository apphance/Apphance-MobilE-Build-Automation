package com.apphance.ameba.plugins.ios.ocunit.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.executor.IOSExecutor
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.AMEBA_IOS_UNIT
import static org.gradle.api.logging.Logging.getLogger

class RunUnitTestsTasks extends DefaultTask {

    private log = getLogger(getClass())

    static String NAME = 'runUnitTests'
    String group = AMEBA_IOS_UNIT
    String description = "Build and executes Unit tests. Requires UnitTests target."

    @Inject IOSExecutor iosExecutor
    @Inject ProjectConfiguration conf
    @Inject IOSUnitTestConfiguration unitTestConf

    @TaskAction
    void runUnitTests() {
        log.lifecycle "Running unit tests with variant: ${unitTestConf.variant.name}"

        def testResults = new File(conf.tmpDir, "test-${unitTestConf.variant.name}.txt")
        testResults.createNewFile()

        iosExecutor.buildTestVariant conf.rootDir, unitTestConf.variant, "${testResults.canonicalPath}".toString()

        File outputUnitTestFile = new File(conf.tmpDir, "TEST-all.xml")
        parseAndExport(testResults, outputUnitTestFile)
    }

    @PackageScope
    void parseAndExport(File testResults, File outputUnitTestFile) {
        OCUnitParser parser = new OCUnitParser()
        parser.parse testResults.text.split('\n').toList()

        new XMLJunitExporter(outputUnitTestFile, parser.testSuites).export()
    }
}
