package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration

import javax.inject.Inject

class AndroidVariantsConfiguration extends Configuration {

    String configurationName = 'Android variants configuration'

    private AndroidConfiguration androidConf
    private List<AndroidVariantConfiguration> variants

    @Inject
    AndroidVariantsConfiguration(AndroidConfiguration androidConf) {
        this.androidConf = androidConf
        this.variants = buildVariantsList()
    }

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

    List<AndroidVariantConfiguration> buildVariantsList() {
        List<AndroidVariantConfiguration> result = []
        if (variantsDirExistsAndIsNotEmpty()) {
            result.addAll(extractVariantsFromDir())
        } else {
            result.addAll(extractDefaultVariants())
        }
        result
    }

    private boolean variantsDirExistsAndIsNotEmpty() {
        File variantsDir = androidConf.variantsDir?.value
        return (variantsDir && variantsDir.isDirectory() && variantsDir.list() > 0)
    }

    private List<AndroidVariantConfiguration> extractVariantsFromDir() {
        File variantsDir = androidConf.variantsDir.value
        //TODO what if a single variant folder is empty, handle?
        variantsDir.listFiles().collect { new AndroidVariantConfiguration((it.name.toLowerCase())) }
    }

    private List<AndroidVariantConfiguration> extractDefaultVariants() {
        AndroidBuildMode.values().collect { new AndroidVariantConfiguration(it.name().toLowerCase()) }
    }

    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        variants
    }
}
