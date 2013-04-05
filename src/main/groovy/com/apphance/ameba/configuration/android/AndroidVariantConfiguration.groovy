package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.StringProperty

class AndroidVariantConfiguration extends Configuration {

    boolean enabled = true
    String variantName

    AndroidVariantConfiguration(String variantName) {
        this.variantName = variantName
    }

    def mode = new StringProperty(
            name: "android.variant.${getVariantName()}.mode",
            message: "Android variant ${getVariantName()} mode",
            possibleValues: { ['Debug', 'Release'] as List<String> }
    )

    def apphanceAppKey = new StringProperty(
            name: "android.variant.${getVariantName()}.apphance.appKey",
            message: "Apphance key for ${getVariantName()}"
    )

    def apphanceMode = new ApphanceModeProperty(
            name: "android.variant.${getVariantName()}.apphance.mode",
            message: "Apphance mode for ${getVariantName()}",
            possibleValues: { ApphanceMode.values()*.name() as List<String> }
    )

    def apphanceLibVersion = new StringProperty(
            name: "android.variant.${getVariantName()}.apphance.lib",
            message: "Apphance lib version for ${getVariantName()}",
    )

    @Override
    String getConfigurationName() {
        "android variant configuration: $variantName"
    }

    private String getVariantName() {
        this.@variantName
    }
}
