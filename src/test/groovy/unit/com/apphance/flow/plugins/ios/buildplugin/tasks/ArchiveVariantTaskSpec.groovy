package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class ArchiveVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('archiveTask', type: ArchiveVariantTask) as ArchiveVariantTask
    def executor = GroovyMock(IOSExecutor)

    def setup() {
        task.executor = executor
    }

    def 'no archive when null variant passed'() {
        given:
        task.variant = null

        when:
        task.build()

        then:
        def e = thrown(NullPointerException)
        e.message == 'Null variant passed to builder!'
    }

    def 'executor runs archive command when variant passed & release conf disabled'() {
        given:
        def variant = GroovySpy(IOSVariant) {
            getTmpDir() >> GroovyMock(File)
            getBuildDir() >> GroovyMock(File) {
                getAbsolutePath() >> 'absolute'
            }
            getConf() >> GroovyMock(IOSConfiguration) {
                xcodebuildExecutionPath() >> ['xcodebuild']
            }
            getName() >> 'GradleXCode'
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            isEnabled() >> false
        }
        task.variant = variant

        when:
        task.build()

        then:
        1 * executor.archiveVariant(_, ['xcodebuild', '-scheme', 'GradleXCode', 'CONFIGURATION_BUILD_DIR=absolute', 'clean', 'build', 'archive'])
    }
}
