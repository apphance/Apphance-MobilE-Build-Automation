package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.cocoapods.PodLocator
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSDeviceArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.info.IOSDeviceArtifactInfo
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.FRAMEWORK
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin(FlowUtils)
class DeviceVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('archiveTask', type: DeviceVariantTask) as DeviceVariantTask

    def setup() {
        task.iosExecutor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> [:]
        }
    }

    def 'exception when null variant passed'() {
        given:
        task.variant = null

        when:
        task.build()

        then:
        def e = thrown(NullPointerException)
        e.message == 'Null variant passed to builder!'
    }

    def 'exception when variant with bad mode passed'() {
        given:
        task.variant = GroovyMock(AbstractIOSVariant) {
            getMode() >> new IOSBuildModeProperty(value: FRAMEWORK)
        }

        when:
        task.build()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid build mode: $FRAMEWORK!"
    }

    @Unroll
    def 'executor runs archive command when variant passed & release conf enabled #releaseConfEnabled'() {
        given:
        def tmpFile = tempFile

        and:
        def variant = GroovySpy(AbstractIOSVariant) {
            getTmpDir() >> GroovyMock(File)
            getName() >> 'GradleXCode'
            getSchemeName() >> 'GradleXCode'
            getSchemeFile() >> tmpFile
            getMode() >> new IOSBuildModeProperty(value: DEVICE)
            getTarget() >> 't'
            getArchiveConfiguration() >> 'c'
            getXcodebuildExecutionPath() >> ['xcodebuild']
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            isEnabled() >> releaseConfEnabled
        }
        task.conf = GroovyMock(IOSConfiguration) {
            getSdk() >> new StringProperty(value: 'iphoneos')
        }
        task.variant = variant
        task.schemeParser = GroovyMock(XCSchemeParser)
        task.artifactProvider = GroovyMock(IOSArtifactProvider)
        task.deviceArtifactsBuilder = GroovyMock(IOSDeviceArtifactsBuilder)
        task.podLocator = GroovyMock(PodLocator)

        when:
        task.build()

        then:
        1 * task.iosExecutor.buildVariant(_, ['xcodebuild', '-scheme', 'GradleXCode', '-sdk', 'iphoneos', 'clean', 'archive']) >> ["FLOW_ARCHIVE_PATH=$temporaryDir.absolutePath"].iterator()
        cnt * task.artifactProvider.deviceInfo(_) >> new IOSDeviceArtifactInfo()
        cnt * task.deviceArtifactsBuilder.buildArtifacts(_)

        where:
        releaseConfEnabled | cnt
        true               | 1
        false              | 0
    }

    def 'null returned when no archive found'() {
        expect:
        task.findArchiveFile([].iterator()) == null
    }

    def 'exception thrown when no archive found'() {
        given:
        def archive = new File('not-existing')

        when:
        task.validateArchiveFile(archive)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.endsWith("Xcarchive file: $archive.absolutePath does not exist or is not a directory")
    }
}
