package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files

import static org.gradle.testfixtures.ProjectBuilder.builder

class ArchiveVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('archiveTask', type: ArchiveVariantTask) as ArchiveVariantTask

    def setup() {
        task.executor = GroovyMock(IOSExecutor)
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
        def tmpFile = Files.createTempFile('a', 'b').toFile()

        and:
        def variant = GroovySpy(IOSVariant) {
            getTmpDir() >> GroovyMock(File)
            getConf() >> GroovyMock(IOSConfiguration) {
                xcodebuildExecutionPath() >> ['xcodebuild']
            }
            getName() >> 'GradleXCode'
            getSchemeFile() >> tmpFile
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            isEnabled() >> false
        }
        task.variant = variant
        task.schemeParser = GroovyMock(XCSchemeParser)

        when:
        task.build()

        then:
        1 * task.executor.archiveVariant(_, ['xcodebuild', '-scheme', 'GradleXCode', 'clean', 'archive']) >> ["FLOW_ARCHIVE_PATH=$tmpFile.absolutePath"].iterator()

        cleanup:
        tmpFile.delete()
    }

    @Ignore
    def 'executor runs archive command and no archive found'() {
        given:
        def variant = GroovySpy(IOSVariant) {
            getTmpDir() >> GroovyMock(File)
            getConf() >> GroovyMock(IOSConfiguration) {
                xcodebuildExecutionPath() >> ['xcodebuild']
            }
            getName() >> 'GradleXCode'
            getSchemeFile() >> GroovyMock(File)
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            isEnabled() >> false
        }
        task.variant = variant
        task.schemeParser = GroovyMock(XCSchemeParser)

        when:
        task.build()

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith('Impossible to find archive file:')
        e.message.endsWith("for variant: $variant.name")
    }
}
