package com.apphance.flow.plugins.ios.ocunit.tasks

import groovy.xml.MarkupBuilder

/**
 * Exports XML JUnit-like result from text output of OCUnit.
 *
 */
class XMLJunitExporter {
    Collection<OCUnitTestSuite> testSuites
    File outputFile

    XMLJunitExporter(File outputFile, Collection<OCUnitTestSuite> testSuites) {
        this.testSuites = testSuites
        this.outputFile = outputFile
    }

    void export() {
        outputFile.withWriter("utf-8", {
            def xml = new MarkupBuilder(it)
            xml.testsuites {
                this.testSuites.each { testSuite -> processTestSuite(xml, testSuite) }
            }
        })
    }

    void processTestSuite(MarkupBuilder xml, OCUnitTestSuite testSuite) {
        xml.testsuite(
                errors: 0,
                failures: testSuite.failureCount,
                hostname: 'autobuild',
                time: testSuite.duration,
                name: testSuite.name) {
            testSuite.testSuites.each { ts -> processTestSuite(xml, ts) }
            testSuite.testCases.each { testCase ->
                xml.testcase(
                        classname: testSuite.name,
                        name: testCase.name,
                        time: testCase.duration) {
                    if (testCase.result == OCUnitTestResult.FAILURE) {
                        testCase.errors.each { error ->
                            xml.failure(
                                    message: error.errorMessage,
                                    type: "Failure",
                                    "${error.file}:${error.line}: ")
                        }
                    }
                }
            }
        }
    }
}
