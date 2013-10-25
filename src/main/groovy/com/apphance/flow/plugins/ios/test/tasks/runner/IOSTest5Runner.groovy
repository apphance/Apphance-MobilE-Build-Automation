package com.apphance.flow.plugins.ios.test.tasks.runner

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitTestSuite
import com.apphance.flow.util.FlowUtils
import groovy.transform.PackageScope

import static java.text.MessageFormat.format

@Mixin(FlowUtils)
class IOSTest5Runner extends AbstractIOSTestRunner {

    @Override
    void runTests(AbstractIOSVariant variant) {
        super.runTests(variant)

        def output = tempFile
        def cmd = ['script', '-q', '-t', '0', output] + variant.xcodebuildExecutionPath +
                ['-scheme', variant.schemeName, '-sdk', 'iphonesimulator', 'test'] as List<String>
        executor.runTests5(variant.tmpDir, cmd)

        Collection<OCUnitTestSuite> parsedResults = parseResults(output?.readLines())

        def testResultsXml = newFile('xml')
        parseAndExport(parsedResults, testResultsXml)

        verifyTestResults(parsedResults, errorMessage(testResultsXml))
    }

    @PackageScope
    String errorMessage(File parsedResults) {
        format(bundle.getString('exception.ios.test5'), variant.name, variant.schemeName,
                fileLinker.fileLink(parsedResults))
    }
}
