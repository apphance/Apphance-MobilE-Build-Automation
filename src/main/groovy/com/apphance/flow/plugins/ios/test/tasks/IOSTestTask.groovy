package com.apphance.flow.plugins.ios.test.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.TEST_ACTION
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class IOSTestTask extends DefaultTask {

    String group = FLOW_TEST
    String description = 'Build and executes iOS tests'

    @Inject IOSConfiguration conf
    @Inject IOSExecutor executor
    @Inject IOSTestPbxEnhancer testPbxEnhancer
    @Inject XCSchemeParser schemeParser
    @Inject PbxJsonParser pbxJsonParser

    IOSVariant variant

    @TaskAction
    void test() {
        logger.info("Running unit tests with variant: $variant.name")

        def testBlueprintIds = schemeParser.findActiveTestableBlueprintIds(variant.schemeFile)
        testPbxEnhancer.addShellScriptToBuildPhase(variant, testBlueprintIds)

        def testTargets = testBlueprintIds.collect { pbxJsonParser.targetForBlueprintId(variant.pbxFile, it) }
        def testConf = schemeParser.configuration(variant.schemeFile, TEST_ACTION)

        testTargets.each { String testTarget ->
            def testResults = createNewTestResultFile(testTarget)
            executor.runTests(variant.tmpDir, testTarget, testConf, testResults.absolutePath)
            parseAndExport(testResults, new File(variant.tmpDir, "${filename(testTarget)}.xml"))
        }
    }

    @PackageScope
    File createNewTestResultFile(String target) {
        def results = new File(variant.tmpDir, "${filename(target)}.log")
        results.delete()
        results.createNewFile()
        results
    }

    @PackageScope
    String filename(String target) {
        "test-${variant.name}-${target}"
    }

    @PackageScope
    void parseAndExport(File testResults, File outputUnitTestFile) {
        OCUnitParser parser = new OCUnitParser()
        parser.parse testResults.text.split('\n').toList()

        new XMLJunitExporter(outputUnitTestFile, parser.testSuites).export()
    }
}
