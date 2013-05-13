package com.apphance.ameba.configuration.android.variants

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.android.AndroidBuildMode
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.ameba.configuration.apphance.ApphanceMode.DISABLED
import static org.gradle.api.logging.Logging.getLogger

class AndroidVariantConfiguration extends AbstractConfiguration {

    def log = getLogger(getClass())

    final String name

    @Inject
    AndroidConfiguration conf
    @Inject
    ApphanceConfiguration apphanceConf

    @Inject
    AndroidVariantConfiguration(@Assisted String name) {
        this.name = name
    }

    @Inject
    @Override
    void init() {
        log.info("Initializing $name")
        apphanceAppKey.name = "android.variant.${name}.apphance.appKey"
        apphanceAppKey.message = "Apphance key for '$name'"
        apphanceMode.name = "android.variant.${name}.apphance.mode"
        apphanceMode.message = "Apphance mode for '$name'"
        apphanceLibVersion.name = "android.variant.${name}.apphance.lib"
        apphanceLibVersion.message = "Apphance lib version for '$name'"

        super.init()
    }

    String getName() {
        this.@name
    }

    AndroidBuildMode getMode() {
        this.@name.toLowerCase().contains(DEBUG.lowerCase()) ? DEBUG : RELEASE
    }

    def apphanceMode = new ApphanceModeProperty(
            interactive: { apphanceConf.enabled },
            required: { apphanceConf.enabled },
            possibleValues: { possibleApphanceModes() },
            validator: { it in possibleApphanceModes() }
    )

    def apphanceAppKey = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            required: { apphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') }
    )

    private List<String> possibleApphanceModes() {
        ApphanceMode.values()*.name() as List<String>
    }

    def apphanceLibVersion = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+') }
    )

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    String getConfigurationName() {
        "Android Variant ${this.@name}"
    }

    File getTmpDir() {
        new File(conf.tmpDir, name)
    }

}
