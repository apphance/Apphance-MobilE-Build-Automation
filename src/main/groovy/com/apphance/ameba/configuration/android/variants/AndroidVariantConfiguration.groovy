package com.apphance.ameba.configuration.android.variants

import com.apphance.ameba.configuration.android.AndroidBuildMode
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.variants.AbstractVariant
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.AssistedInject

import javax.inject.Inject

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.ameba.util.file.FileManager.relativeTo

class AndroidVariantConfiguration extends AbstractVariant {

    final String prefix = 'android'

    private File vDir

    @AssistedInject
    AndroidVariantConfiguration(@Assisted String name) {
        super(name.capitalize())
    }

    @AssistedInject
    AndroidVariantConfiguration(@Assisted String name, @Assisted File variantDir) {
        super(name.capitalize())
        this.vDir = variantDir
    }

    @Override
    @Inject
    void init() {
        variantDir.name = "android.variant.${name}.dir"
        super.init()
        if (!variantDir.value && vDir)
            variantDir.value = relativeTo(conf.rootDir.absolutePath, vDir.absolutePath)
    }

    AndroidBuildMode getMode() {
        name.toLowerCase().contains(DEBUG.lowerCase()) ? DEBUG : RELEASE
    }

    def variantDir = new FileProperty(
            interactive: { false },
            //validator: { it in releaseConf.findMobileProvisionFiles()*.name }TODO
    )

    @Override
    String getConfigurationName() {
        "Android Variant ${name}"
    }
}
