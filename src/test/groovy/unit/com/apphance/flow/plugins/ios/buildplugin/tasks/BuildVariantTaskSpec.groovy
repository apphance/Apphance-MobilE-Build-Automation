package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class BuildVariantTaskSpec extends Specification {

    def p = builder().build()
    def task = p.task('task', type: BuildVariantTask) as BuildVariantTask

    def setup() {
        task.conf = GroovyMock(IOSConfiguration) {
            xcodebuildExecutionPath() >> ['xcodebuild']
            getSimulatorSdk() >> new StringProperty(value: 'iphonesimulator')
            getSdk() >> new StringProperty(value: 'iphoneos')
        }
    }

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
            getMode() >> new IOSBuildModeProperty(value: DEVICE)
            getName() >> 'scheme1'
        }

        when:
        task.build()

        then:
        noExceptionThrown()
        1 * task.executor.buildVariant(tmpDir, ['xcodebuild', '-scheme', 'scheme1', '-sdk', 'iphoneos', 'clean', 'build'])
    }

    @Unroll
    def 'build cmd is constructed correctly for mode #mode'() {
        given:
        task.variant = GroovyMock(IOSVariant) {
            getMode() >> new IOSBuildModeProperty(value: mode)
            getName() >> 'scheme1'
        }

        expect:
        task.cmd.join(' ') == expected

        where:
        mode      | expected
        SIMULATOR | 'xcodebuild -scheme scheme1 -sdk iphonesimulator -arch i386 clean build'
        DEVICE    | 'xcodebuild -scheme scheme1 -sdk iphoneos clean build'
    }
}
