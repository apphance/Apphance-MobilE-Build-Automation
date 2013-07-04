package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
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
        task.archiveVariant()

        then:
        0 * _._
    }

    def 'executor runs archive command when variant passed'() {
        given:
        def variant = GroovySpy(IOSVariant) {
            getTmpDir() >> GroovyMock(File)
            getBuildDir() >> GroovyMock(File) {
                getAbsolutePath() >> 'absolute'
            }
            getConf() >> GroovyMock(IOSConfiguration) {
                xcodebuildExecutionPath() >> ['xcodebuild']
            }
            getConfiguration() >> 'BasicConfiguration'
            getName() >> 'GradleXCode'
        }
        task.variant = variant

        when:
        task.archiveVariant()

        then:
        1 * executor.archiveVariant(_, ['xcodebuild', '-scheme', 'GradleXCode', 'CONFIGURATION_BUILD_DIR=absolute', 'archive'])
    }
}
