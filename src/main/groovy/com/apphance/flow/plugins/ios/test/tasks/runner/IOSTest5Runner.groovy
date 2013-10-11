package com.apphance.flow.plugins.ios.test.tasks.runner

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitTestSuite
import groovy.transform.PackageScope

class IOSTest5Runner extends AbstractIOSTestRunner {

    @Override
    void runTests(AbstractIOSVariant variant) {
        super.runTests(variant)

        def cmd = variant.xcodebuildExecutionPath + ['-scheme', variant.schemeName, '-sdk', 'iphonesimulator', 'test'] as List<String>
        def output = executor.runTests5(variant.tmpDir, cmd)

        Collection<OCUnitTestSuite> parsedResults = parseResults(output?.toList())

        def testResultsXml = newFile('xml')
        parseAndExport(parsedResults, testResultsXml)

        verifyTestResults(parsedResults, errorMessage(testResultsXml))
    }

    @PackageScope
    //TODO to validation properties
    String errorMessage(File parsedResults) {
        "Error while executing tests for variant: $variant.name, scheme: $variant.schemeName. " +
                "For further details investigate test results: ${fileLinker.fileLink(parsedResults)}"
    }
}
