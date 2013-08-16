package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Shared
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class BuildVariantTaskSpec extends Specification {

    @Shared
    def p = builder().build()
    @Shared
    def task = p.task('task', type: BuildVariantTask) as BuildVariantTask

    def 'exception thrown when null variant passed'() {
        when:
        task.build()

        then:
        def e = thrown(NullPointerException)
        e.message == 'Null variant passed to builder!'
    }

    def 'executor builds variant'() {
        given:
        def tmpDir = createTempDir()
        tmpDir.deleteOnExit()

        task.executor = GroovyMock(IOSExecutor)
        task.variant = GroovyMock(IOSVariant) {
            getTmpDir() >> tmpDir
            getBuildCmd() >> ['xcodebuild', 'variant']
        }

        when:
        task.build()

        then:
        noExceptionThrown()
        1 * task.executor.buildVariant(tmpDir, ['xcodebuild', 'variant'])
    }
}
