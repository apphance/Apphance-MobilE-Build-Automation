package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration

import javax.inject.Inject

class IOSUnitTestConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Unit Test Configuration'
    private enabledInternal = false

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
