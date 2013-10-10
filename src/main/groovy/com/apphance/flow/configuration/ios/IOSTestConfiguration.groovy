package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.scheme.XCSchemeInfo
import com.apphance.flow.util.FlowUtils
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

/**
 * Test configuration keeps the list of variants configured to run tests against them.
 */
@Singleton
@Mixin(FlowUtils)
class IOSTestConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Test Configuration'
    private enabledInternal = false

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject XCSchemeInfo schemeInfo
    @Inject IOSExecutor executor

    @Inject
    @Override
    void init() {
        super.init()
    }

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def testVariantsNames = new ListStringProperty(
            name: 'ios.test.variants',
            message: 'iOS test variants',
            doc: { docBundle.getString('ios.test.variants') },
            possibleValues: { possibleTestVariants },
            validator: {
                def list = testVariantsNames.convert(it.toString())
                list.size() == list.unique().size() && !list.isEmpty() && list.every { it in possibleTestVariants }
            },
            required: { true }
    )

    @Lazy
    @PackageScope
    List<String> possibleTestVariants = {
        variantsConf.variants.findAll {
            schemeInfo.schemeHasEnabledTestTargets(it.schemeFile)
        }*.name
    }()

    @Lazy
    @PackageScope
    List<AbstractIOSVariant> testVariants = {
        variantsConf.variants.findAll { it.name in testVariantsNames.value }
    }()

    @Override
    boolean canBeEnabled() {
        iosSimInstalled && hasEnabledTestTargets
    }

    @Lazy
    @PackageScope
    boolean iosSimInstalled = {
        def iosSimVersion = executor.iOSSimVersion
        isNotEmpty(iosSimVersion) && isNotBlank(iosSimVersion)
    }()

    @Lazy
    @PackageScope
    boolean hasEnabledTestTargets = {
        schemeInfo.schemesHasEnabledTestTargets()
    }()

    @Override
    String explainDisabled() {
        "'${configurationName}' cannot be enabled. ${explainIOSSim()}${explainNoTestTargets()}"
    }

    @PackageScope
    String explainIOSSim() {
        iosSimInstalled ? '' : 'Ios-sim is not installed. '
    }

    @PackageScope
    String explainNoTestTargets() {
        hasEnabledTestTargets ? '' : 'No schemes with test targets enabled detected. '
    }

    @Override
    void checkProperties() {
        defaultValidation testVariantsNames
    }
}
