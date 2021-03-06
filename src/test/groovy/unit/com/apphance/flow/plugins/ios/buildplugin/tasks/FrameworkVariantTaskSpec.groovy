package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSFrameworkArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class FrameworkVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('frameworkTask', type: FrameworkVariantTask) as FrameworkVariantTask

    def 'framework action is invoked with all interactions'() {
        given:
        task.conf = GroovyStub(IOSConfiguration) {
            getSimulatorSdk() >> new StringProperty(value: '')
            getSdk() >> new StringProperty(value: '')
        }
        task.iosExecutor = GroovyMock(IOSExecutor)
        task.variant = GroovyMock(AbstractIOSVariant) {
            getName() >> 'variant'
            getSchemeName() >> 'variant'
            getArchiveConfiguration() >> 'archive'
            getFrameworkHeaders() >> new ListStringProperty(value: [])
            getFrameworkResources() >> new ListStringProperty(value: [])
            getFrameworkLibs() >> new ListStringProperty(value: [])
            getMode() >> new IOSBuildModeProperty(value: FRAMEWORK)
            getXcodebuildExecutionPath() >> ['xcodebuild']
        }
        task.artifactProvider = GroovyMock(IOSArtifactProvider) {
            frameworkInfo(_) >> new IOSFrameworkArtifactInfo()
        }
        task.frameworkArtifactsBuilder = GroovyMock(IOSFrameworkArtifactsBuilder)
        task.fu = new FlowUtils()

        when:
        task.build()

        then:
        1 * task.iosExecutor.buildVariant(null, ['xcodebuild', '-scheme', 'variant', '-sdk', 'iphoneos',
                '-configuration', 'archive', "CONFIGURATION_BUILD_DIR=$task.deviceTmpDir.absolutePath", 'PRODUCT_NAME=device', 'clean', 'build'])
        1 * task.iosExecutor.buildVariant(null, ['xcodebuild', '-scheme', 'variant', '-sdk', 'iphonesimulator',
                '-configuration', 'archive', "CONFIGURATION_BUILD_DIR=$task.simTmpDir.absolutePath", 'PRODUCT_NAME=sim',
                'clean', 'build'])
        1 * task.artifactProvider.frameworkInfo(_) >> new IOSFrameworkArtifactInfo()
        1 * task.frameworkArtifactsBuilder.buildArtifacts(_)
    }

    def 'exception thrown when null variant passed'() {
        given:
        task.variant = null

        when:
        task.build()

        then:
        def e = thrown(NullPointerException)
        e.message == 'Null variant passed to builder!'
    }

    def 'exception thrown when variant with bad mode passed'() {
        given:
        task.variant = GroovyMock(AbstractIOSVariant) {
            getMode() >> new IOSBuildModeProperty(value: mode)
        }

        when:
        task.build()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid build mode: $mode!"

        where:
        mode << [DEVICE, SIMULATOR]
    }
}
