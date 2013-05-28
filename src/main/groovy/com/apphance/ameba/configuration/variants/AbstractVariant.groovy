package com.apphance.ameba.configuration.variants

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.apphance.ameba.configuration.apphance.ApphanceMode.DISABLED

abstract class AbstractVariant extends AbstractConfiguration {

    final String name

    @Inject ProjectConfiguration conf
    @Inject ApphanceConfiguration apphanceConf

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
            validator: { it in possibleApphanceModes() }
    )

    def apphanceAppKey = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            required: { apphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') }
    )

    List<String> possibleApphanceModes() {
        ApphanceMode.values()*.name() as List<String>
    }

    def apphanceLibVersion = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+') }
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
        println "${conf} ${conf?.tmpDir} $name"
        new File(conf.tmpDir, name)
    }

    abstract String getPrefix()
}
