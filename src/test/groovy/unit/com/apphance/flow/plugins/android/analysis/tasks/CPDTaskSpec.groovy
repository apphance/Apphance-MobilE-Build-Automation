package com.apphance.flow.plugins.android.analysis.tasks

import com.apphance.flow.TestUtils
import org.gradle.api.plugins.quality.Pmd
import spock.lang.Specification

@Mixin(TestUtils)
class CPDTaskSpec extends Specification {

    def task = create CPDTask

    def 'cpd task is of type Pmd'() {
        expect:
        task instanceof Pmd
    }

    def 'cpd task runs ant'() {
        given:
        task.runner = GroovyMock(CPDTask.CPDRunner)

        when:
        task.run()

        then:
        1 * task.runner.runAnt(task.project)
    }
}
