package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSSchemeInfo
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

@Mixin(FlowUtils)
class IOSTestConfigurationSpec extends Specification {

    def 'can be enabled according to version'() {
        given:
        def tc = new IOSTestConfiguration(
                executor: GroovyMock(IOSExecutor) {
                    getxCodeVersion() >> xCodeVersion
                    getiOSSimVersion() >> iosSimVersion
                },
                schemeInfo: GroovyMock(IOSSchemeInfo) {
                    schemesHasEnabledTestTargets() >> hasTestTargets
                }
        )

        expect:
        tc.canBeEnabled() == canBeEnabled

        where:
        xCodeVersion | iosSimVersion | hasTestTargets || canBeEnabled
        '4.6.2'      | '1.5.3'       | true           || true
        '4'          | '1.5.4'       | false          || false
        '3'          | ''            | true           || false
        '4.5.7'      | ''            | false          || false
        '5'          | '2.3.4'       | true           || false
        '6.0.1'      | '1.5.2'       | false          || false
        '5.0.1'      | ''            | true           || false
        '7'          | ''            | false          || false
    }

    def 'xCode version lower than the border'() {
        given:
        def tc = new IOSTestConfiguration(executor: GroovyMock(IOSExecutor) {
            getxCodeVersion() >> xCodeVersion
        })

        expect:
        tc.xCodeVersionLowerThanBorder == lower

        where:
        xCodeVersion || lower
        '4.6.2'      || true
        '5'          || false
        '6.2.3'      || false
    }

    def 'ios-sim is installed'() {
        given:
        def tc = new IOSTestConfiguration(executor: GroovyMock(IOSExecutor) {
            getiOSSimVersion() >> iosSimVersion
        })

        expect:
        tc.iosSimInstalled == installed

        where:
        iosSimVersion || installed
        '1.5.2'       || true
        ''            || false
    }

    def 'no test targets explanation'() {
        given:
        def tc = new IOSTestConfiguration(schemeInfo: GroovyMock(IOSSchemeInfo) {
            schemesHasEnabledTestTargets() >> enabled
        })

        expect:
        explanation == tc.explainNoTestTargets()

        where:
        enabled || explanation
        true    || ''
        false   || 'No schemes with test targets enabled detected. '
    }

    def 'wrong xcode version explanation'() {
        given:
        def tc = new IOSTestConfiguration(executor: GroovyMock(IOSExecutor) {
            getxCodeVersion() >> version
        })

        expect:
        explanation == tc.explainXCodeVersion()

        where:
        version || explanation
        '4.6.2' || ''
        '5'     || "Testing is supported for xCode version lower than 5. "
    }

    def 'no ios-sim explanation'() {
        given:
        def tc = new IOSTestConfiguration(executor: GroovyMock(IOSExecutor) {
            getiOSSimVersion() >> version
        })

        expect:
        explanation == tc.explainIOSSim()

        where:
        version || explanation
        '1.5.3' || ''
        ''      || 'Ios-sim is not installed. '
    }

    def 'possible test variants found'() {
        given:
        def projectDir = new File(IOSVariantsConfiguration.class.getResource('iOSProject/xcshareddata/xcschemes').toURI())
        def tc = new IOSTestConfiguration(
                variantsConf: GroovyMock(IOSVariantsConfiguration) {
                    getVariants() >> projectDir.listFiles().collect { file ->
                        GroovyMock(IOSVariant) {
                            getSchemeFile() >> file
                            getName() >> getNameWithoutExtension(file.name)
                        }
                    }
                },
                schemeInfo: new IOSSchemeInfo(schemeParser: new XCSchemeParser())
        )

        expect:
        tc.possibleTestVariants == ['GradleXCode', 'GradleXCodeWithApphance']
    }

    def 'possible test variants validator works correctly'() {
        given:
        def tc = GroovySpy(IOSTestConfiguration)
        tc.possibleTestVariants >> ['Variant1', 'Variant2']

        expect:
        tc.testVariantsNames.validator(tv) == valid

        where:
        tv                       || valid
        ['Variant1']             || true
        ['Variant1', 'Variant1'] || false
        ['Variant1', 'Variant3'] || false
        []                       || false
    }
}
