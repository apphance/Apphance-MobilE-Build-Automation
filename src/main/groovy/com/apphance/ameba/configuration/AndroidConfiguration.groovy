package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID

@com.google.inject.Singleton
class AndroidConfiguration extends Configuration {

    AndroidConfiguration() {
    }

    @Inject
    ProjectConfiguration conf

    def enabled = false
    int order = 1

    String configurationName = "Android configuration"

    @Override
    boolean isEnabled() {
        enabled && conf.enabled && conf.type != null && conf.type == ANDROID
    }

    @Override
    void setEnabled(boolean enabled) {
        this.enabled = enabled
    }

    def sdkDir = new FileProperty(
            name: 'android.sdk.dir',
            message: 'Android SDK directory')

    def minSdkTargetName = new StringProperty(
            name: 'android.min.sdk.target.name',
            message: 'Android min SDK target name')

}
