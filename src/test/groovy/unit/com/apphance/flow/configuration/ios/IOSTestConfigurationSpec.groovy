package com.apphance.flow.configuration.ios

import com.apphance.flow.plugins.ios.scheme.XCSchemeInfo
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.scheme.XCSchemeInfo
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
                schemeInfo: GroovyMock(XCSchemeInfo) {
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
        def tc = new IOSTestConfiguration(schemeInfo: GroovyMock(XCSchemeInfo) {
            schemesHasEnabledTestTargets() >> enabled
        })

        expect:
        explanation == tc.explainNoTestTargets()

        where:
        enabled || explanation
        true    || ''
        false   || 'No schemes with test targets enabled detected. '
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
        def projectDir = new File(XCSchemeInfo.class.getResource('iOSProject/xcshareddata/xcschemes').toURI())
        def tc = new IOSTestConfiguration(
                variantsConf: GroovyMock(IOSVariantsConfiguration) {
                    getVariants() >> projectDir.listFiles().collect { file ->
                        GroovyMock(AbstractIOSVariant) {
                            getSchemeFile() >> file
                            getName() >> getNameWithoutExtension(file.name)
                        }
                    }
                },
                schemeInfo: new XCSchemeInfo(schemeParser: new XCSchemeParser())
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
