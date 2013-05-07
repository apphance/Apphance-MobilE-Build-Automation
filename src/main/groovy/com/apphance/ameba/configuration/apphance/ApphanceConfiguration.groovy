package com.apphance.ameba.configuration.apphance

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

@com.google.inject.Singleton
class ApphanceConfiguration extends AbstractConfiguration {

    String configurationName = 'Apphance Configuration'
    private boolean enabledInternal = false

    @Inject
    ProjectConfiguration conf

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
