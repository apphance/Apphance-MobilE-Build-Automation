package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.executor.AntExecutor
import spock.lang.Specification

@Mixin(TestUtils)
class RunRobolectricTestsTaskSpec extends Specification {

    def task = create RunRobolectricTestsTask

    def 'test runRobolectricTests'() {
        given:
        def tempVariantDir = temporaryDir
        new File(tempVariantDir, 'build.xml') << new File("src/test/resources/com/apphance/flow/android/$buildFile").text

        task.variantConf = GroovyStub(AndroidVariantConfiguration) {
            getTmpDir() >> tempVariantDir
        }
        task.antExecutor = Mock(AntExecutor)
        assert new File(tempVariantDir, 'build.xml').text.findAll('target name="test"').size() == initialTestTasks

        when:
        task.runRobolectricTests()

        then:
        new File(tempVariantDir, 'build.xml').text.findAll('target name="test"').size() == 1
        1 * task.antExecutor.executeTarget(tempVariantDir, 'test')

        where:
        buildFile                    | initialTestTasks
        'build-with-robolectric.xml' | 1
        'build.xml'                  | 0
    }
}
