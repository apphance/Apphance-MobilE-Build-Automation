package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

class IOSApphanceConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Apphance Configuration'
    private boolean enabledInternal = false

    @Inject
    IOSConfiguration conf

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    def user = new StringProperty(
            name: 'ios.apphance.user',
            message: 'Apphance user'
    )

    def pass = new StringProperty(
            name: 'ios.apphance.pass',
            message: 'Apphance pass'
    )
}
