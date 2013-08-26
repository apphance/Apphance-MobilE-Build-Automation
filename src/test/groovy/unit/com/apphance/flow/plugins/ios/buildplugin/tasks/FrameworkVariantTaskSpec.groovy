package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSFrameworkArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testfixtures.ProjectBuilder.builder

class FrameworkVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('frameworkTask', type: FrameworkVariantTask) as FrameworkVariantTask

    @Unroll
    def 'framework action is invoked, release conf enabled: #releaseConfEnabled'() {
        given:
        task.conf = GroovyMock(IOSConfiguration) {
            xcodebuildExecutionPath() >> ['xcodebuild']
            getSimulatorSdk() >> new StringProperty(value: '')
            getSdk() >> new StringProperty(value: '')
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            isEnabled() >> releaseConfEnabled
        }
        task.iosExecutor = GroovyMock(IOSExecutor)
        task.variant = GroovyMock(IOSVariant) {
            getName() >> 'variant'
            getArchiveConfiguration() >> 'archive'
            getFrameworkHeaders() >> new ListStringProperty(value: [])
            getFrameworkResources() >> new ListStringProperty(value: [])
        }
        task.artifactProvider = GroovyMock(IOSArtifactProvider)
        task.frameworkArtifactsBuilder = GroovyMock(IOSFrameworkArtifactsBuilder)
        task.fu = GroovyMock(FlowUtils) {
            getTempDir() >> GroovyMock(File)
        }

        when:
        task.build()

        then:
        1 * task.iosExecutor.buildVariant(null, ['xcodebuild', '-scheme', 'variant', '-sdk', 'iphoneos',
                '-configuration', 'archive', 'CONFIGURATION_BUILD_DIR=null', 'PRODUCT_NAME=device', 'clean', 'build'])
        1 * task.iosExecutor.buildVariant(null, ['xcodebuild', '-scheme', 'variant', '-sdk', 'iphonesimulator',
                '-arch', 'i386', '-configuration', 'archive', 'CONFIGURATION_BUILD_DIR=null', 'PRODUCT_NAME=sim',
                'clean', 'build'])
        invocationCount * task.artifactProvider.frameworkInfo(_) >> new IOSFrameworkArtifactInfo()
        invocationCount * task.frameworkArtifactsBuilder.buildArtifacts(_)

        where:
        releaseConfEnabled | invocationCount
        false              | 0
        true               | 1
    }
}
