package com.apphance.flow.configuration.android.variants

interface AndroidVariantFactory {

    AndroidVariantConfiguration create(String name)

    AndroidVariantConfiguration create(String name, File variantDir)
}