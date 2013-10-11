package com.apphance.flow.plugins.ios.test.tasks.runner

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.test.tasks.pbx.IOSTestPbxEnhancer
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitTestSuite
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.XCAction.TEST_ACTION

class IOSTestLT5Runner extends AbstractIOSTestRunner {

    @Inject IOSTestPbxEnhancer testPbxEnhancer
    @Inject XCSchemeParser schemeParser
    @Inject PbxJsonParser pbxJsonParser

    @Override
    void runTests(AbstractIOSVariant variant) {
        super.runTests(variant)

        def testBlueprintIds = schemeParser.findActiveTestableBlueprintIds(variant.schemeFile)
        testPbxEnhancer.addShellScriptToBuildPhase(variant, testBlueprintIds)

        def testTargets = testBlueprintIds.collect { pbxJsonParser.targetForBlueprintId.call(variant.pbxFile, it) }
        def testConf = schemeParser.configuration(variant.schemeFile, TEST_ACTION)

        testTargets.each { String testTarget ->

            def testResultsLog = newFile('log', testTarget)
            def cmd = variant.xcodebuildExecutionPath + ['-target', testTarget, '-configuration', testConf, '-sdk', 'iphonesimulator', 'clean', 'build'] as List<String>
            executor.runTestsLT5(variant.tmpDir, cmd, testResultsLog.absolutePath)

            Collection<OCUnitTestSuite> parsedResults = parseResults(testResultsLog.readLines())

            def testResultsXml = newFile('xml', testTarget)
            parseAndExport(parsedResults, testResultsXml)

            verifyTestResults(parsedResults, errorMessage(testTarget, testConf, testResultsXml))
        }
    }

    @PackageScope
    //TODO to validation properties
    String errorMessage(String target, String configuration, File parsedResults) {
        "Error while executing tests for variant: $variant.name, target: $target, configuration $configuration. " +
                "For further details investigate test results: ${fileLinker.fileLink(parsedResults)}"
    }
}
