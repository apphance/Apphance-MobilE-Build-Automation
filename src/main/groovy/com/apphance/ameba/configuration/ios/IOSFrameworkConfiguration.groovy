package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration

import javax.inject.Inject

class IOSFrameworkConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Framework Configuration'
    private boolean enabledInternal = false

    @Inject
    IOSConfiguration conf

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }
}
