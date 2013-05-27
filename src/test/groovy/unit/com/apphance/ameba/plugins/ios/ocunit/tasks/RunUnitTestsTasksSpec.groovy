package com.apphance.ameba.plugins.ios.ocunit.tasks

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.IOSExecutor
import com.google.common.io.Files
import spock.lang.Specification

@Mixin(TestUtils)
class RunUnitTestsTasksSpec extends Specification {

    def runUnitTestTask = create RunUnitTestsTasks

    def variant = GroovyStub(AbstractIOSVariant)
    def iosExecutor = GroovyMock(IOSExecutor)
    def unitTestConf = GroovyMock(IOSUnitTestConfiguration) { getVariant() >> variant }

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
