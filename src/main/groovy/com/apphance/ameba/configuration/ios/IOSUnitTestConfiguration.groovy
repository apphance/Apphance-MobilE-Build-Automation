package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

@com.google.inject.Singleton
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

    def configuration = new StringProperty(
            name:'ios.unitTests.configuration',
            message: 'IOS unit test configuration'
    )

    def target = new StringProperty(
            name:'ios.unitTests.target',
            message: 'IOS unit test target'
    )
}
