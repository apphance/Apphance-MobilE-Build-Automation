package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

@com.google.inject.Singleton
class IOSUnitTestConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Unit Test Configuration'
    private enabledInternal = false

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration iosVariantsConf

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def variant = new StringProperty(
            name: 'ios.unitTests.variant',
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
        check conf.tmpDir.exists(), "Tmp directory ${conf.tmpDir.absolutePath} doesn't exist"
        check variant.validator(variant.value), "Variant value ${variant.value} is incorrect"
    }
}
