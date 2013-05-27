package com.apphance.ameba.plugins.ios.ocunit.tasks

import com.apphance.ameba.TestUtils
import spock.lang.Ignore
import spock.lang.Specification

@Mixin(TestUtils)
class XMLJunitExporterSpec extends Specification {

    def runUnitTestTask = create RunUnitTestsTasks

    @Ignore("Need example file with test result")
    def 'generate export'() {
        given:
        File testResults // = ???
        File outputUnitTestFile = File.createTempFile('prefix', 'suffix')
        outputUnitTestFile.deleteOnExit()

        when:
        runUnitTestTask.parseAndExport(testResults, outputUnitTestFile)

        then:
        // some assertions on
        outputUnitTestFile.text
    }
}
