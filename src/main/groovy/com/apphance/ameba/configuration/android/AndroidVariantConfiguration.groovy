package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.properties.StringProperty

class AndroidVariantConfiguration extends Configuration {

    boolean enabled = true
    String variantName

    AndroidVariantConfiguration(String variantName) {
        this.variantName = variantName
    }

    def mode = new StringProperty(
            name: "android.variant.${getVariantName()}.mode",
            message: "Android variant $variantName mode",
            possibleValues: { ['Debug', 'Release'] as List<String> }
    )

    @Override
    String getConfigurationName() {
        "android variant configuration: $variantName"
    }

    String getVariantName() {
        return variantName
    }
}
