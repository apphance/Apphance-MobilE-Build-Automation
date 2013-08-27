package com.apphance.flow.plugins.android.analysis.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.executor.AndroidExecutor
import spock.lang.Specification

@Mixin(TestUtils)
class LintTaskSpec extends Specification {

    def task = create LintTask

    def 'test lint task runs lint command'() {
        given:
        def taskDir = temporaryDir
        task.androidExecutor = GroovyMock(AndroidExecutor)
        task.androidVariantsConf = GroovyStub(AndroidVariantsConfiguration) {
            getMain() >> GroovyStub(AndroidVariantConfiguration) {
                getTmpDir() >> taskDir
            }
        }

        when:
        task.run()

        then:
        1 * task.androidExecutor.runLint(taskDir, new File(task.project.rootDir, 'build/reports/lint/lint-raport.html'))
    }
}
