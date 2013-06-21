package com.apphance.flow.configuration.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED

abstract class AbstractVariant extends AbstractConfiguration {

    final String name

    @Inject ProjectConfiguration conf
    @Inject ApphanceConfiguration apphanceConf
    @Inject AndroidReleaseConfiguration androidReleaseConf

    @Inject
    AbstractVariant(@Assisted String name) {
        this.name = name
    }

    @Inject
    @Override
    void init() {

        apphanceAppKey.name = "${prefix}.variant.${name}.apphance.appKey"
        apphanceAppKey.message = "Apphance key for '$name'"
        apphanceMode.name = "${prefix}.variant.${name}.apphance.mode"
        apphanceMode.message = "Apphance mode for '$name'"
        apphanceLibVersion.name = "${prefix}.variant.${name}.apphance.lib"
        apphanceLibVersion.message = "Apphance lib version for '$name'"

        super.init()
    }

    def apphanceMode = new ApphanceModeProperty(
            interactive: { apphanceConf.enabled },
            required: { apphanceConf.enabled },
            possibleValues: { possibleApphanceModes() },
            validator: { ApphanceMode it -> it.toString() in possibleApphanceModes() }
    )

    def apphanceAppKey = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            required: { apphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') },
            validationMessage: "key should match '[a-z0-9]+'"
    )

    List<String> possibleApphanceModes() {
        ApphanceMode.values()*.name() as List<String>
    }

    //TODO add default
    //TODO add possible
    def apphanceLibVersion = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+(-[^-]*)?') }
    )

    String getName() {
        this.@name
    }

    String getBuildTaskName() {
        "build$name"
    }

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    File getTmpDir() {
        new File(conf.tmpDir, name)
    }

    abstract String getPrefix()

    @Override
    void checkProperties() {
        if (apphanceConf.enabled) {
            defaultValidation apphanceMode, apphanceAppKey, apphanceLibVersion
        }
    }
}
