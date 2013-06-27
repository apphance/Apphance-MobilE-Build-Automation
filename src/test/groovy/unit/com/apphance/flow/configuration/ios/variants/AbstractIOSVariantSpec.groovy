package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PlistParser
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR

class AbstractIOSVariantSpec extends Specification {

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
    }

    def 'apphance lib dependency is constructed correctly'() {
        given:
        def variant = GroovySpy(AbstractIOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.2')
            getTarget() >> 't'
            getConfiguration() >> 'c'
        }
        variant.executor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> ['ARCHS': 'armv6 armv7']
        }
        variant.apphanceArtifactory = GroovyMock(ApphanceArtifactory) {
            iOSArchs(_) >> ['armv6', 'armv7']
        }

        expect:
        variant.apphanceDependencyArch() == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'armv7'
        SILENT       | 'armv7'
        PROD         | 'armv7'
    }

    def 'validates version code and version string when empty'() {
        given:
        def variant = GroovySpy(AbstractIOSVariant) {
            getConf() >> GroovyMock(IOSConfiguration) {
                getExtVersionCode() >> versionCode
            }
            getPlist() >> GroovyMock(File)
            getTarget() >> ''
            getConfiguration() >> ''
        }
        variant.apphanceConf = GroovyMock(ApphanceConfiguration) {
            isEnabled() >> false
        }
        variant.plistParser = GroovyMock(PlistParser) {
            evaluate(_, _, _) >> ''
            versionCode(_) >> ''
        }

        when:
        def errors = variant.verify()

        then:
        noExceptionThrown()

        and:
        errors.size() == expectedSize
        verifyErrors.call(errors)

        where:
        versionCode | expectedSize | verifyErrors
        ''          | 2            | {
            it.find { m -> m.contains("Property versionCode must have numerical value!") } && it.find { m ->
                m.contains("Property versionString must not have whitespace characters!")
            }
        }
        '3145'      | 1            | {
            it.find { m ->
                m.contains("Property versionString must not have whitespace characters!")
            }
        }
    }
}
