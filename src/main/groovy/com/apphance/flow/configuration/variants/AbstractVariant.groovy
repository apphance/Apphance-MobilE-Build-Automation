package com.apphance.flow.configuration.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.detection.project.ProjectType
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static com.apphance.flow.configuration.properties.BooleanProperty.POSSIBLE_BOOLEAN
import static org.apache.commons.lang.StringUtils.isEmpty

abstract class AbstractVariant extends AbstractConfiguration {

    final String name

    @Inject ProjectConfiguration conf
    @Inject ApphanceConfiguration apphanceConf
    @Inject ApphanceArtifactory apphanceArtifactory
    private boolean enabledInternal = true

    @Inject
    AbstractVariant(@Assisted String name) {
        this.name = name
    }

    @Inject
    @Override
    void init() {

        aphMode.name = "${projectType.prefix}.variant.${name}.apphance.mode"
        aphMode.message = "Apphance mode for '$name'"
        aphAppKey.name = "${projectType.prefix}.variant.${name}.apphance.appKey"
        aphAppKey.message = "Apphance key for '$name'"
        aphLib.name = "${projectType.prefix}.variant.${name}.apphance.lib"
        aphLib.message = "Apphance lib version for '$name'"
        aphWithUTest.name = "${projectType.prefix}.variant.${name}.apphance.withUTest"
        aphWithUTest.message = "Apphance withUTest property for '$name'"
        aphReportOnShake.name = "${projectType.prefix}.variant.${name}.apphance.reportOnShake"
        aphReportOnShake.message = "Apphance reportOnShake property for '$name'"
        aphDefaultUser.name = "${projectType.prefix}.variant.${name}.apphance.defaultUser"
        aphDefaultUser.message = "Apphance defaultUser property for '$name'"
        aphWithScreenShotsFromGallery.name = "${projectType.prefix}.variant.${name}.apphance.withScreenShotsFromGallery"
        aphWithScreenShotsFromGallery.message = "Apphance withScreenShotsFromGallery property for '$name'"
        aphReportOnDoubleSlide.name = "${projectType.prefix}.variant.${name}.apphance.reportOnDoubleSlide"
        aphReportOnDoubleSlide.message = "Apphance reportOnDoubleSlide property for '$name'"
        aphAppVersionName.name = "${projectType.prefix}.variant.${name}.apphance.appVersionName"
        aphAppVersionName.message = "Apphance appVersionName property for '$name'"
        aphAppVersionCode.name = "${projectType.prefix}.variant.${name}.apphance.appVersionCode"
        aphAppVersionCode.message = "Apphance appVersionCode property for '$name'"
        aphMachException.name = "${projectType.prefix}.variant.${name}.apphance.machException"
        aphMachException.message = "Apphance machException property for '$name'"

        displayName.name = "${projectType.prefix}.variant.${name}.display.name"

        aphAppKey.doc = { docBundle.getString('variant.apphance.appKey') }
        aphLib.doc = { docBundle.getString('variant.apphance.lib') }

        super.init()
    }

    def aphMode = new ApphanceModeProperty(
            interactive: { apphanceEnabled },
            required: { apphanceConf.enabled },
            possibleValues: { possibleApphanceModes },
            validator: { it -> it.toString() in possibleApphanceModes }
    )

    @Lazy
    List<String> possibleApphanceModes = {
        ApphanceMode.values()*.name() as List<String>
    }()

    def aphAppKey = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) },
            required: { apphanceConf.enabled },
            validator: { it?.matches('[a-z0-9]+') },
            validationMessage: "Key should match '[a-z0-9]+'"
    )

    def aphLib = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) },
            possibleValues: { possibleApphanceLibVersions },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+(-[^-]*)?') }
    )

    abstract List<String> getPossibleApphanceLibVersions()

    def aphWithUTest = new BooleanProperty(
            defaultValue: { false },
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) },
            possibleValues: { POSSIBLE_BOOLEAN },
            validator: { isEmpty(it) ? true : it in POSSIBLE_BOOLEAN }
    )

    def aphReportOnShake = new BooleanProperty(
            defaultValue: { true },
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) },
            possibleValues: { POSSIBLE_BOOLEAN },
            validator: { isEmpty(it) ? true : it in POSSIBLE_BOOLEAN }
    )

    def aphDefaultUser = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) },
    )

    def aphWithScreenShotsFromGallery = new BooleanProperty(
            defaultValue: { false },
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) },
            possibleValues: { POSSIBLE_BOOLEAN },
            validator: { isEmpty(it) ? true : it in POSSIBLE_BOOLEAN }
    )

    def aphReportOnDoubleSlide = new BooleanProperty(
            defaultValue: { false },
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) && isIOS() },
            possibleValues: { POSSIBLE_BOOLEAN },
            validator: { isEmpty(it) ? true : it in POSSIBLE_BOOLEAN }
    )

    def aphAppVersionName = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) && isIOS() }
    )

    def aphAppVersionCode = new StringProperty(
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) && isIOS() },
    )

    def aphMachException = new BooleanProperty(
            defaultValue: { false },
            interactive: { apphanceEnabled && !(DISABLED == aphMode.value) && isIOS() },
            possibleValues: { POSSIBLE_BOOLEAN },
            validator: { isEmpty(it) ? true : it in POSSIBLE_BOOLEAN }
    )

    @Lazy
    @PackageScope
    boolean apphanceEnabled = {
        def enabled = apphanceConf.enabled && apphanceEnabledForVariant
        if (!enabled)
            aphMode.value = DISABLED
        enabled
    }()

    @Lazy
    @PackageScope
    boolean apphanceEnabledForVariant = {
        true
    }()

    @PackageScope
    boolean isIOS() {
        projectType == ProjectType.IOS
    }

    private displayName = new StringProperty(
            doc: { docBundle.getString('variant.display.name') },
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
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    @Override
    String getEnabledPropKey() {
        "${projectType.prefix}.variant.${name}.enabled"
    }

    File getTmpDir() {
        new File(conf.tmpDir, name)
    }

    abstract ProjectType getProjectType()

    @Override
    void validate(List<String> errors) {
        if (apphanceConf.enabled) {
            errors.addAll(propValidator.validateProperties(aphMode))
            if (aphMode.value != DISABLED) {
                errors.addAll(propValidator.validateProperties(aphAppKey, aphLib))
            }
        }
    }
}
