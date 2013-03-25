package com.apphance.ameba.configuration

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID

class AndroidConfiguration implements  Configuration {

    @Inject
    ProjectConfiguration conf

    def enabled = false

    int order = 20

    String pluginName = "Android plugin"

    @Override
    List<AmebaProperty> getAmebaProperties() {
        [
                new AmebaProperty(name: 'android.sdk.dir', message: 'Android SDK directory'),
                new AmebaProperty(name: 'android.target.name', message: 'Target name'),
//                new AmebaProperty(name: 'android.', message: '', defaultValue: {}),
        ]
    }

    @Override
    boolean isEnabled() {
        enabled && conf.enabled && conf.type != null && conf.type == ANDROID
    }

    @Override
    void setEnabled(boolean enabled) {
        this.enabled = enabled
    }
}
