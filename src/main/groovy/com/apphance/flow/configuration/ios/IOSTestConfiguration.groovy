package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.util.Version
import com.google.inject.Singleton

import javax.inject.Inject

import static org.apache.commons.lang.StringUtils.isNotEmpty

@Singleton
class IOSTestConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Unit Test Configuration'
    private enabledInternal = false

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration iosVariantsConf
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

    def variant = new StringProperty(
            name: 'ios.unit.test.variant',
            message: 'IOS unit test variant',
            possibleValues: { iosVariantsConf.variantsNames.value },
            validator: { it in iosVariantsConf.variantsNames.value },
            required: { true }
    )

    IOSVariant getVariant() {
        iosVariantsConf.variants.find { it.name == this.@variant.value }
    }

    @Override
    boolean canBeEnabled() {
        new Version(executor.xCodeVersion).compareTo(BORDER_VERSION) < 0 &&
                isNotEmpty(executor.iOSSimVersion)
    }

    //TODO xcode version
    //TODO ios-sim version
    //TODO no test targets detected
    @Override
    String explainDisabled() {
        "'${configurationName}' cannot be enabled because testing is supported for xCode version lower than 5. Current version is: ${executor.xCodeVersion}"
    }

    @Override
    void checkProperties() {
        super.checkProperties()
        defaultValidation variant
    }
}
