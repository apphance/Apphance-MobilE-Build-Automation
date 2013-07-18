package com.apphance.flow.plugins.ios.ocunit.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.ios.IOSTestConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.google.common.io.Files
import spock.lang.Specification

@Mixin(TestUtils)
class RunUnitTestsTasksSpec extends Specification {

    def runUnitTestTask = create RunUnitTestsTasks

    def variant = GroovyStub(IOSVariant)
    def iosExecutor = GroovyMock(IOSExecutor)
    def unitTestConf = GroovyMock(IOSTestConfiguration) { getVariant() >> variant }

    private File dir = Files.createTempDir()

    def setup() {
        dir.deleteOnExit()
        variant.name >> "VariantName"
        runUnitTestTask.conf = GroovyStub(ProjectConfiguration) { getTmpDir() >> dir }
        runUnitTestTask.iosExecutor = iosExecutor
        runUnitTestTask.unitTestConf = unitTestConf
    }

    def 'run test for variant'() {
        when:
        runUnitTestTask.runUnitTests()

        then:
        1 * iosExecutor.buildTestVariant(_, variant, new File(dir, "test-${unitTestConf.variant.name}.txt").canonicalPath)
        new File(dir, "TEST-all.xml").exists()
    }
}
