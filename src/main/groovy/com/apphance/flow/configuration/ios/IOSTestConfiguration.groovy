package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class IOSTestConfiguration extends AbstractConfiguration {

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

    IOSVariant getVariant() {
        iosVariantsConf.variants.find { it.name == this.@variant.value }
    }

    @Override
    void checkProperties() {
        super.checkProperties()
        defaultValidation variant
    }
}
