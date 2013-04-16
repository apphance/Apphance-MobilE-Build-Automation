package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

@com.google.inject.Singleton
class AndroidApphanceConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Apphance Configuration'
    private boolean enabled = false

    private AndroidConfiguration androidConf

    @Inject
    AndroidApphanceConfiguration(AndroidConfiguration androidConf) {
        this.androidConf = androidConf
    }

    @Override
    boolean isEnabled() {
        this.@enabled
    }

    @Override
    void setEnabled(boolean enabled) {
        this.@enabled = enabled
    }

    @Override
    boolean isActive() {
        this.@enabled && androidConf.active
    }

    def user = new StringProperty(
            name: 'android.apphance.user',
            message: 'Apphance user'
    )

    def pass = new StringProperty(
            name: 'android.apphance.pass',
            message: 'Apphance pass'
    )

    def key = new StringProperty(
            name: 'android.apphance.key',
            message: 'Apphance key'
    )
}
