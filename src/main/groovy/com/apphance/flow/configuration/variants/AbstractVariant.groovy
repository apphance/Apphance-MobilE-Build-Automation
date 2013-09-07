package com.apphance.flow.configuration.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED

abstract class AbstractVariant extends AbstractConfiguration {

    final String name

    @Inject ProjectConfiguration conf
    @Inject ApphanceConfiguration apphanceConf
    @Inject ApphanceArtifactory apphanceArtifactory

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

        displayName.name = "${prefix}.variant.${name}.display.name"

        super.init()
    }

    def apphanceMode = new ApphanceModeProperty(
            interactive: { apphanceEnabled },
            required: { apphanceConf.enabled },
            possibleValues: { possibleApphanceModes },
            validator: { it -> it.toString() in possibleApphanceModes }
    )

    @Lazy
    List<String> possibleApphanceModes = {
        ApphanceMode.values()*.name() as List<String>
    }()

    def apphanceAppKey = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == apphanceMode.value) },
            required: { apphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') },
            validationMessage: "Key should match '[a-z0-9]+'"
    )

    def apphanceLibVersion = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == apphanceMode.value) },
            possibleValues: { possibleApphanceLibVersions },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+(-[^-]*)?') }
    )

    abstract List<String> getPossibleApphanceLibVersions()

    @Lazy
    @PackageScope
    boolean apphanceEnabled = {
        def enabled = apphanceConf.enabled && apphanceEnabledForVariant
        if (!enabled)
            apphanceMode.value = DISABLED
        enabled
    }()

    @Lazy
    boolean apphanceEnabledForVariant = {
        true
    }()

    private displayName = new StringProperty(
            required: { false },
            interactive: { false }
    )

    StringProperty getDisplayName() {
        new StringProperty(value: displayName?.value ?: name?.replaceAll('[-_]', ' '))
    }

    String getName() {
        this.@name
    }

    String getUploadTaskName() {
        "upload$name".replaceAll('\\s', '')
    }

    String getTestTaskName() {
        "test$name".replaceAll('\\s', '')
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
            defaultValidation apphanceMode
            if (apphanceMode.value != DISABLED) {
                defaultValidation apphanceAppKey, apphanceLibVersion
            }
        }
    }
}
