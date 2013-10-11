package com.apphance.flow.plugins.ios.test.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.test.tasks.runner.IOSTest5Runner
import com.apphance.flow.plugins.ios.test.tasks.runner.IOSTestLT5Runner
import com.apphance.flow.util.Version
import spock.lang.Shared
import spock.lang.Specification

@Mixin(TestUtils)
class IOSTestTaskSpec extends Specification {

    @Shared
    def task = create(IOSTestTask)

    def 'appropriate test runner is invoked for xcode version'() {
        given:
        task.executor = GroovyStub(IOSExecutor) {
            getxCodeVersion() >> version
        }
        task.test5Runner = GroovyMock(IOSTest5Runner)
        task.testLT5Runner = GroovyMock(IOSTestLT5Runner)
        task.variant = GroovyStub(AbstractIOSVariant)


        when:
        task.xcodeVersion = new Version(version)
        task.test()

        then:
        cntlt5 * task.testLT5Runner.runTests(_)
        cnt5 * task.test5Runner.runTests(_)

        where:
        version | cntlt5 | cnt5
        '4.9'   | 1      | 0
        '5.0'   | 0      | 1
        '5.1'   | 0      | 1
    }

//    def setupSpec() {
//        task.fileLinker = new SimpleFileLinker()
//        task.variant = GroovyMock(AbstractIOSVariant) {
//            getName() >> 'v1'
//        }
//    }
}
