package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import com.google.inject.Inject

@com.google.inject.Singleton
class AndroidJarLibraryConfiguration extends AbstractConfiguration {

    final String configurationName = 'Android Jar Library Configuration'

    private boolean enabledInternal = false

    private AndroidConfiguration androidConfiguration

    @Inject
    AndroidJarLibraryConfiguration(AndroidConfiguration androidConfiguration) {
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

    def resourcePrefix = new StringProperty(
            name: 'android.jarLibrary.resPrefix',
            message: 'Internal directory name used to embed resources in the jar',
            defaultValue: { '' }
    )
}
