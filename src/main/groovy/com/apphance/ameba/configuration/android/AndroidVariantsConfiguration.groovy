package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.google.inject.Inject

class AndroidVariantsConfiguration extends Configuration {

    String configurationName = 'Android variants configuration'

    @Inject
    AndroidConfiguration androidConf

    @Override
    boolean isEnabled() {
        androidConf.enabled
    }

    @Override
    void setEnabled(boolean enabled) {
        //this configuration is always enabled
        //even if user did not specified variants
        //there are two basic variants: release and debug
        throw new IllegalStateException("${configurationName} is always enabled")
    }

    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        //TODO getVariansName()
        List<AndroidVariantConfiguration> result = []
        if (androidConf.variantsDir.value?.exists()) {
            androidConf.variantsDir.value.listFiles().each {
                if (it.isDirectory()) {
                    def avc = new AndroidVariantConfiguration(it.name)
                    result << avc
                }
            }
        } else {
            ['Debug', 'Release'].each {
                def avc = new AndroidVariantConfiguration(it)
                result << avc
            }
        }
        result
    }
}
