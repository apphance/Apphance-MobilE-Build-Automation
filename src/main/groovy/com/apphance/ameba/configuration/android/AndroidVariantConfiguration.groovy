package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyPersister

import static com.apphance.ameba.configuration.apphance.ApphanceMode.DISABLED

class AndroidVariantConfiguration extends AbstractConfiguration {

    final String name
    private AndroidConfiguration androidConf
    private AndroidApphanceConfiguration androidApphanceConf

    AndroidVariantConfiguration(String name,
                                PropertyPersister persister,
                                AndroidConfiguration androidConf,
                                AndroidApphanceConfiguration androidApphanceConf) {
        this.propertyPersister = persister
        this.name = name
        this.androidConf = androidConf
        this.androidApphanceConf = androidApphanceConf

        initFields()
    }

    private void initFields() {
        mode.name = "android.variant.${name}.mode"
        mode.message = "Android variant '$name' mode"
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

    def mode = new StringProperty(
            possibleValues: { possibleModes() },
            defaultValue: { AndroidBuildMode.DEBUG.name() },
            validator: { it in possibleModes() },
            required: { true }
    )

    private List<String> possibleModes() {
        AndroidBuildMode.values()*.name() as List<String>
    }

    def apphanceAppKey = new StringProperty(
            interactive: { androidApphanceConf.enabled },
            required: { androidApphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') }
    )

    def apphanceMode = new ApphanceModeProperty(
            interactive: { androidApphanceConf.enabled },
            required: { androidApphanceConf.enabled },
            possibleValues: { possibleApphanceModes() },
            validator: { it in possibleApphanceModes() }
    )

    private List<String> possibleApphanceModes() {
        ApphanceMode.values()*.name() as List<String>
    }

    def apphanceLibVersion = new StringProperty(
            interactive: { androidApphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+') }
    )

    @Override
    boolean isEnabled() {
        androidConf.enabled
    }

    @Override
    String getConfigurationName() {
        "Android configuration variant ${this.@name}"
    }

    File getTmpDir() {
        new File(androidConf.tmpDir, name)
    }

}
