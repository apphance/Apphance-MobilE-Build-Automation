package com.apphance.flow.plugins.ios.ocunit.tasks

import com.apphance.flow.TestUtils
import spock.lang.Specification

@Mixin(TestUtils)
class XMLJunitExporterSpec extends Specification {

    def 'generate export'() {
        given:
        File testResults = new File('src/test/resources/com/apphance/flow/plugins/ios/ocunit/tasks/test_output.txt')
        File outputUnitTestFile = File.createTempFile('prefix', 'suffix')
        outputUnitTestFile.deleteOnExit()

        expect:
        testResults.exists()

        when:
        OCUnitParser parser = new OCUnitParser()
        parser.parse testResults.text.split('\n').toList()
        new XMLJunitExporter(outputUnitTestFile, parser.testSuites).export()
        def testsuites = new XmlSlurper().parseText outputUnitTestFile.text

        then:
        testsuites.testsuite.size() == 1
        def rootTestSuite = testsuites.testsuite[0]
        rootTestSuite.@name.text() == 'All tests'

        rootTestSuite.testsuite.size() == 92

        def testQWAddCommentMethodExecutorSpec = rootTestSuite.testsuite.find { it.@name.text() == 'QWAddCommentMethodExecutorSpec' }
        testQWAddCommentMethodExecutorSpec.@failures.text() == '1'
        def failedTest = testQWAddCommentMethodExecutorSpec.testcase.find {
            it.@name.text() == 'QWAddCommentMethodExecutor_ExecutingRequest_WhenRequestFailed_ShouldRevertPopularityOfPhoto'
        }
        failedTest.failure
        failedTest.failure.@message.text().contains('QWAddCommentMethodExecutor, executing request, when request failed, should revert popularity of photo')

        def testQWAddCommentParserSpec = rootTestSuite.testsuite.find { it.@name.text() == 'QWAddCommentParserSpec' }
        testQWAddCommentParserSpec.@failures.text() == '0'
        testQWAddCommentParserSpec.testcase.findAll {it.@classname.text() == 'QWAddCommentParserSpec'}.size() == 6
    }
}
