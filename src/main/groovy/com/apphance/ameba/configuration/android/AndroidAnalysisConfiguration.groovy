package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.URLProperty

import javax.inject.Inject

@com.google.inject.Singleton
class AndroidAnalysisConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Analysis Configuration'
    private boolean enabledInternal = false

    private AndroidConfiguration androidConfiguration

    @Inject
    AndroidAnalysisConfiguration(AndroidConfiguration androidConfiguration) {
        this.androidConfiguration = androidConfiguration
    }

    @Override
    boolean isEnabled() {
        enabledInternal && androidConfiguration.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def analysisConfigUrl = new URLProperty(
            name: 'android.analysis.config.url',
            message: 'Android analysis config URL',
    )

    @Override
    void checkProperties() {
        if (analysisConfigUrl.isSet()) {
            check !checkException { analysisConfigUrl.value }, "Property '${analysisConfigUrl.name}' is not valid! Should be valid URL address!"
        }
    }
}

