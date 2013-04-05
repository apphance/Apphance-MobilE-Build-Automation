package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.properties.StringProperty
import com.google.inject.Inject

class AndroidJarLibraryConfiguration extends Configuration{

    final String configurationName = 'Android jar library configuration'

    boolean enabled = false

    @Inject AndroidConfiguration androidConfiguration

    @Override
    boolean isEnabled() {
        enabled && androidConfiguration.enabled
    }

    def resourcePrefix = new StringProperty(
            name: 'android.jarLibrary.resPrefix',
            message: 'Internal directory name used to embed resources in the jar',
            defaultValue: {''}
    )
}
