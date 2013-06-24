package com.apphance.flow.configuration.apphance

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class ApphanceConfiguration extends AbstractConfiguration {

    String configurationName = 'Apphance Configuration'
    private boolean enabledInternal = false

    @Inject ProjectConfiguration conf

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def user = new StringProperty(
            name: 'apphance.user',
            message: 'Apphance user'
    )

    def pass = new StringProperty(
            name: 'apphance.pass',
            message: 'Apphance pass'
    )
}
