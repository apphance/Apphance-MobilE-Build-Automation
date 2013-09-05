package com.apphance.flow.plugins.ios.test.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.test.tasks.pbx.IOSTestPbxEnhancer
import com.apphance.flow.plugins.ios.test.tasks.results.exporter.XMLJunitExporter
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitParser
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitTestSuite
import com.apphance.flow.util.Preconditions
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.TEST_ACTION
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

@Mixin(Preconditions)
class IOSTestTask extends DefaultTask {

    String group = FLOW_TEST
    String description = 'Build and executes iOS tests'

    @Inject IOSConfiguration conf
    @Inject IOSExecutor executor
    @Inject IOSTestPbxEnhancer testPbxEnhancer
    @Inject XCSchemeParser schemeParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject FileLinker fileLinker

    IOSVariant variant

    @TaskAction
    void test() {
        logger.info("Running unit tests with variant: $variant.name")

        def testBlueprintIds = schemeParser.findActiveTestableBlueprintIds(variant.schemeFile)
        testPbxEnhancer.addShellScriptToBuildPhase(variant, testBlueprintIds)

        def testTargets = testBlueprintIds.collect { pbxJsonParser.targetForBlueprintId.call(variant.pbxFile, it) }
        def testConf = schemeParser.configuration(variant.schemeFile, TEST_ACTION)

        testTargets.each { String testTarget ->

            def testResultsLog = newFile(testTarget, 'log')
            executor.runTests(variant.tmpDir, testTarget, testConf, testResultsLog.absolutePath)

            Collection<OCUnitTestSuite> parsedResults = parseResults(testResultsLog)

            def testResultsXml = newFile(testTarget, 'xml')
            parseAndExport(parsedResults, testResultsXml)

            verifyTestResults(parsedResults, errorMessage(testTarget, testConf, testResultsXml))
        }
    }

    @PackageScope
    File newFile(String target, String extension) {
        def results = new File(variant.tmpDir, "${filename(target)}.$extension")
        results.delete()
        results.createNewFile()
        results
    }

    @PackageScope
    String filename(String target) {
        "test-$variant.name-$target"
    }

    @PackageScope
    Collection<OCUnitTestSuite> parseResults(File testResults) {
        OCUnitParser parser = new OCUnitParser()
        parser.parse testResults.readLines()
        parser.testSuites
    }

    @PackageScope
    void parseAndExport(Collection<OCUnitTestSuite> testSuites, File outputFile) {
        new XMLJunitExporter(outputFile, testSuites).export()
    }

    @PackageScope
    void verifyTestResults(Collection<OCUnitTestSuite> ocUnitTestSuites, String errorMessage) {
        throwIfConditionTrue(ocUnitTestSuites.any { it.failureCount > 0 }, errorMessage)
    }

    @PackageScope
    String errorMessage(String target, String configuration, File parsedResults) {
        "Error while executing tests for variant: $variant.name, target: $target, configuration $configuration. " +
                "For further details investigate test results: ${fileLinker.fileLink(parsedResults)}"
    }
}
