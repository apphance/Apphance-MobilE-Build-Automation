package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.StringProperty

import javax.inject.Inject

@com.google.inject.Singleton
class AndroidJarLibraryConfiguration extends AbstractConfiguration {

    final String configurationName = 'Android Jar Library Configuration'

    private boolean enabledInternal = false

    @Inject AndroidConfiguration androidConf
    @Inject AndroidReleaseConfiguration releaseConf

    @Override
    boolean isEnabled() {
        enabledInternal && androidConf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def resourcePrefix = new StringProperty(
            name: 'android.jar.library.resPrefix',
            message: 'Internal directory name used to embed resources in the jar',
            validator: {
                try {
                    def file = new File(androidConf.tmpDir, "${it}-res")
                    return file.mkdirs() || file.directory
                } catch (Exception e) { return false }
            }
    )

    @Override
    boolean canBeEnabled() {
        !releaseConf.enabled
    }

    @Override
    String explainDisabled() {
        "'$configurationName' cannot be enabled because '${releaseConf.configurationName}' is enabled and those plugins are mutually exclusive.\n"
    }

    @Override
    void checkProperties() {
        check resourcePrefix.validator(resourcePrefix.value), "Property ${resourcePrefix.name} is not valid! Can not create '${new File(androidConf.tmpDir, "${resourcePrefix.value}-res")}' directory"
        check !releaseConf.enabled, explainDisabled()
    }
}
