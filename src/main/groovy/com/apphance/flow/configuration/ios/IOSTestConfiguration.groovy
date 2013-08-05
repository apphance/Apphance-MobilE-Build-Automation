package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.variants.IOSSchemeInfo
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.util.FlowUtils
import com.apphance.flow.util.Version
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

@Singleton
@Mixin(FlowUtils)
class IOSTestConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Unit Test Configuration'
    private enabledInternal = false

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSSchemeInfo schemeInfo
    @Inject IOSExecutor executor
    private final BORDER_VERSION = new Version('5')

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
    List<IOSVariant> testVariants = {
        variantsConf.variants.findAll { it.name in testVariantsNames.value }
    }()

    @Override
    boolean canBeEnabled() {
        xCodeVersionLowerThanBorder && iosSimInstalled && hasEnabledTestTargets
    }

    @Lazy
    @PackageScope
    boolean xCodeVersionLowerThanBorder = {
        new Version(executor.xCodeVersion).compareTo(BORDER_VERSION) < 0
    }()

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
        "'${configurationName}' cannot be enabled. ${explainXCodeVersion()}${explainIOSSim()}${explainNoTestTargets()}"
    }

    @PackageScope
    String explainXCodeVersion() {
        xCodeVersionLowerThanBorder ? '' : "Testing is supported for xCode version lower than $BORDER_VERSION. "
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
        super.checkProperties()
        defaultValidation testVariantsNames
    }
}
