package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSSimulatorArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.info.IOSSimArtifactInfo
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static org.gradle.testfixtures.ProjectBuilder.builder

class SimulatorVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('archiveTask', type: SimulatorVariantTask) as SimulatorVariantTask

    def 'simulator build action is invoked with all interactions'() {
        given:
        task.conf = GroovyStub(IOSConfiguration) {
            getSimulatorSdk() >> new StringProperty(value: 'iphonesimulator')
        }
        task.variant = GroovyStub(AbstractIOSVariant) {
            getName() >> 'GradleXCode'
            getSchemeName() >> 'GradleXCode'
            getTmpDir() >> GroovyMock(File)
            getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
            getTarget() >> 't'
            getArchiveConfiguration() >> 'c'
            getXcodebuildExecutionPath() >> ['xcodebuild']
        }
        task.iosExecutor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> [:]
        }
        task.fu = new FlowUtils()
        task.artifactProvider = GroovyMock(IOSArtifactProvider)
        task.simulatorArtifactsBuilder = GroovyMock(IOSSimulatorArtifactsBuilder)

        when:
        task.build()

        then:
        1 * task.iosExecutor.buildVariant(_, ['xcodebuild', '-scheme', 'GradleXCode', '-configuration', 'c', '-sdk', 'iphonesimulator', '-arch', 'i386', "CONFIGURATION_BUILD_DIR=${task.simTmpDir.absolutePath}", 'clean', 'build'])
        1 * task.artifactProvider.simInfo(_) >> new IOSSimArtifactInfo()
        1 * task.simulatorArtifactsBuilder.buildArtifacts(_)
    }
}
