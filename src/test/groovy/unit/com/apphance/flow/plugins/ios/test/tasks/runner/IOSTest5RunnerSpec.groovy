package com.apphance.flow.plugins.ios.test.tasks.runner

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.linker.SimpleFileLinker
import spock.lang.Specification

class IOSTest5RunnerSpec extends Specification {

    def runner = new IOSTest5Runner()

    def 'error message is generated well'() {
        given:
        runner.variant = GroovyStub(AbstractIOSVariant) {
            getName() >> 'v1'
            getSchemeName() >> 's1'
        }
        runner.fileLinker = new SimpleFileLinker()
        def results = new File('results')

        expect:
        runner.errorMessage(results) == "Error while executing tests for variant: v1, scheme: s1. " +
                "For further details investigate test results: $results.absolutePath"

        cleanup:
        results.delete()
    }

    def 'task action is executed with all interactions'() {
        given:
        def tmpDir = new File('tmpDir')
        tmpDir.mkdirs()
        def variant = GroovyStub(AbstractIOSVariant) {
            getName() >> 'v1'
            getSchemeName() >> 's1'
            getTmpDir() >> tmpDir
            getXcodebuildExecutionPath() >> ['xcodebuild']
        }

        and:
        def executor = GroovyMock(IOSExecutor)

        and:
        runner.executor = executor
        runner.fileLinker = new SimpleFileLinker()

        when:
        runner.runTests(variant)

        then:
        1 * executor.runTests5(tmpDir, ['xcodebuild', '-scheme', 's1', '-sdk', 'iphonesimulator', 'test']) >> []

        cleanup:
        tmpDir.deleteDir()
    }
}
