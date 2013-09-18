package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static com.google.common.io.Files.createTempDir

class IOSSchemeVariantSpec extends Specification {

    @Shared
    def tmpDir = createTempDir()

    @Unroll
    def 'apphance enabled depending for #mode'() {
        given:
        def conf = new IOSSchemeVariant('name')
        conf.mode.value = mode

        and:
        conf.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getEnabled() >> true
        }

        expect:
        conf.apphanceEnabled == expected

        where:
        mode      | expected
        DEVICE    | true
        SIMULATOR | false
        FRAMEWORK | false
    }

    def 'apphance lib dependency is constructed correctly'() {
        given:
        def variant = GroovySpy(AbstractIOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.2')
            getTarget() >> 't'
            getArchiveConfiguration() >> 'a'
        }
        variant.executor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> ['ARCHS': 'armv6 armv7']
        }
        variant.apphanceArtifactory = GroovyMock(ApphanceArtifactory) {
            iOSArchs(_) >> ['armv6', 'armv7']
        }

        expect:
        variant.apphanceDependencyArch == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'armv7'
        SILENT       | 'armv7'
        PROD         | 'armv7'
    }

    def 'possible mobile provision paths found'() {
        given:
        def conf = GroovyMock(IOSConfiguration) { getRootDir() >> new File('demo/ios/GradleXCode') }
        def releaseConf = new IOSReleaseConfiguration(conf: conf)
        def variant = new IOSSchemeVariant('v')
        variant.conf = conf
        variant.releaseConf = releaseConf

        expect:
        variant.possibleMobileProvisionPaths == ['release/distribution_resources/GradleXCode.mobileprovision']
    }
}
