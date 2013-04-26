package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyPersister

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE
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
        this.@name.toLowerCase().contains(DEBUG.name().toLowerCase()) ? DEBUG : RELEASE
    }

    def apphanceMode = new ApphanceModeProperty(
            interactive: { androidApphanceConf.enabled },
            required: { androidApphanceConf.enabled },
            possibleValues: { possibleApphanceModes() },
            validator: { it in possibleApphanceModes() }
    )

    def apphanceAppKey = new StringProperty(
            interactive: { androidApphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            required: { androidApphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') }
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
