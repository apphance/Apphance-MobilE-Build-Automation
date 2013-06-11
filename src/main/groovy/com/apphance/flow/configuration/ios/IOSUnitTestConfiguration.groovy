package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty

import javax.inject.Inject

@com.google.inject.Singleton
class IOSUnitTestConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Unit Test Configuration'
    private enabledInternal = false

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration iosVariantsConf

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

    AbstractIOSVariant getVariant() {
        iosVariantsConf.variants.find { it.name == this.@variant.value }
    }

    @Override
    void checkProperties() {
        super.checkProperties()
        defaultValidation variant
    }
}
