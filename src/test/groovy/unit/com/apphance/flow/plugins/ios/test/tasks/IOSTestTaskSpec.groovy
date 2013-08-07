package com.apphance.flow.plugins.ios.test.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.linker.SimpleFileLinker
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitTestSuite
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification

@Mixin(TestUtils)
class IOSTestTaskSpec extends Specification {

    @Shared
    def task = create(IOSTestTask)

    def setupSpec() {
        task.fileLinker = new SimpleFileLinker()
        task.variant = GroovyMock(IOSVariant) {
            getName() >> 'v1'
        }
    }

    def 'error message is generated well'() {
        given:
        def results = new File('results')

        expect:
        task.errorMessage('t1', 'c1', results) == "Error while executing tests for variant: v1, target: t1," +
                " configuration c1. For further details investigate test results: $results.absolutePath"

        cleanup:
        results.delete()
    }

    def 'exception thrown when failed present'() {
        when:
        task.verifyTestResults([GroovyMock(OCUnitTestSuite) {
            getFailureCount() >> 1
        }], 'error message')

        then:
        def e = thrown(GradleException)
        e.message == 'error message'
    }

    def 'no exception thrown when all tests passed'() {
        when:
        task.verifyTestResults([GroovyMock(OCUnitTestSuite) {
            getFailureCount() >> 0
        }], 'error message')

        then:
        noExceptionThrown()
    }

}
