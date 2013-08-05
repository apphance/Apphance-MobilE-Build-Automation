package com.apphance.flow.plugins.ios.ocunit.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class IOSTestTask extends DefaultTask {

    String group = FLOW_TEST
    String description = 'Build and executes Unit tests'

    @Inject IOSConfiguration conf
    @Inject IOSExecutor executor

    IOSVariant variant

    @TaskAction
    void test() {
        logger.lifecycle "Running unit tests with variant: $variant.name"

        def testResults = new File(variant.tmpDir, "test-${variant.name}.txt")
        testResults.createNewFile()

        executor.buildTestVariant variant.tmpDir, variant, "${testResults.canonicalPath}".toString()

        File outputUnitTestFile = new File(conf.tmpDir, 'TEST-all.xml')
        parseAndExport(testResults, outputUnitTestFile)
    }

    @PackageScope
    void parseAndExport(File testResults, File outputUnitTestFile) {
        OCUnitParser parser = new OCUnitParser()
        parser.parse testResults.text.split('\n').toList()

        new XMLJunitExporter(outputUnitTestFile, parser.testSuites).export()
    }
}
