package com.apphance.ameba.configuration.android.variants

import com.apphance.ameba.configuration.android.AndroidBuildMode
import com.apphance.ameba.configuration.variants.AbstractVariant
import com.google.inject.assistedinject.Assisted
import com.google.inject.internal.util.$Nullable

import javax.inject.Inject

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE

class AndroidVariantConfiguration extends AbstractVariant {

    final String prefix = 'android'
    final File variantDir

    @Inject
    AndroidVariantConfiguration(@Assisted String name, @Assisted @$Nullable File variantDir = null) {
        super(name)
        this.variantDir = variantDir
    }

    AndroidBuildMode getMode() {
        name.toLowerCase().contains(DEBUG.lowerCase()) ? DEBUG : RELEASE
    }

    @Override
    String getConfigurationName() {
        "Android Variant ${name}"
    }
}
