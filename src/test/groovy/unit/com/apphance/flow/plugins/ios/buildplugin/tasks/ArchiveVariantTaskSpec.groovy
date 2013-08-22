package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.IOSSimulatorArtifactsBuilder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static org.gradle.testfixtures.ProjectBuilder.builder

class ArchiveVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('archiveTask', type: ArchiveVariantTask) as ArchiveVariantTask

    def setup() {
        task.iosExecutor = GroovyMock(IOSExecutor)
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
            getMode() >> new IOSBuildModeProperty(value: DEVICE)
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            isEnabled() >> false
        }
        task.conf = GroovyMock(IOSConfiguration) {
            xcodebuildExecutionPath() >> ['xcodebuild']
            getSdk() >> new StringProperty(value: 'iphoneos')
        }
        task.variant = variant
        task.schemeParser = GroovyMock(XCSchemeParser)

        when:
        task.build()

        then:
        noExceptionThrown()
        1 * task.iosExecutor.buildVariant(_, ['xcodebuild', '-scheme', 'GradleXCode', '-sdk', 'iphoneos', 'clean', 'archive']) >> ["FLOW_ARCHIVE_PATH=$tmpFile.absolutePath"].iterator()

        cleanup:
        tmpFile.delete()
    }

    def 'exception thrown when no archive found'() {
        when:
        task.findArchiveFile(["FLOW_ARCHIVE_PATH=none"].iterator())

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith('Xcarchive file: ')
        e.message.endsWith('none does not exist or is not a directory')
    }

    @Unroll
    def 'correct instance of builder is returned for mode #mode'() {
        given:
        task.simulatorArtifactsBuilder = GroovyMock(IOSSimulatorArtifactsBuilder)
        task.deviceArtifactsBuilder = GroovyMock(IOSDeviceArtifactsBuilder)

        and:
        task.variant = GroovyMock(IOSVariant) {
            getMode() >> new IOSBuildModeProperty(value: mode)
        }

        expect:
        task.builder.call().class.name.contains(name)

        where:
        mode      | name
        DEVICE    | 'Device'
        SIMULATOR | 'Simulator'
    }

    def 'no builder found for bad variant'() {
        given:
        task.variant = GroovyMock(IOSVariant) {
            getMode() >> new IOSBuildModeProperty(value: '')
            getName() >> 'V1'
        }

        when:
        task.builder.call()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Unknown build mode \'null\' for variant \'V1\''
    }
}
