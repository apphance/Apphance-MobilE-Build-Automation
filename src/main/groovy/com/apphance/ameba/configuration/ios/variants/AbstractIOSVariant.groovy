package com.apphance.ameba.configuration.ios.variants

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.apphance.ameba.configuration.apphance.ApphanceMode.DISABLED

abstract class AbstractIOSVariant extends AbstractConfiguration {

    @Inject
    IOSConfiguration conf
    @Inject
    IOSReleaseConfiguration releaseConf
    @Inject
    ApphanceConfiguration apphanceConf
    @Inject
    PlistParser plistParser
    @Inject
    PbxJsonParser pbxJsonParser
    @Inject
    PropertyReader reader

    final String name

    @Inject
    AbstractIOSVariant(@Assisted String name) {
        this.name = name
    }

    @Override
    @Inject
    void init() {

        apphanceAppKey.name = "ios.variant.${name}.apphance.appKey"
        apphanceAppKey.message = "Apphance key for '$name'"
        apphanceMode.name = "ios.variant.${name}.apphance.mode"
        apphanceMode.message = "Apphance mode for '$name'"
        apphanceLibVersion.name = "ios.variant.${name}.apphance.lib"
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

    private List<String> possibleApphanceModes() {
        ApphanceMode.values()*.name() as List<String>
    }

    def apphanceLibVersion = new StringProperty(
            interactive: { apphanceConf.enabled && !(DISABLED == apphanceMode.value) },
            validator: { it?.matches('([0-9]+\\.)*[0-9]+') }
    )

    def mobileprovision = new FileProperty(
            interactive: { releaseConf.enabled },
            required: { releaseConf.enabled },
            possibleValues: { releaseConf.findMobileProvisionFiles()*.name as List<String> },
            validator: { it in releaseConf.findMobileProvisionFiles()*.name }
    )

    File getTmpDir() {
        new File(conf.tmpDir, name)
    }

    @Override
    String getConfigurationName() {
        "iOS Variant ${this.@name}"
    }

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    String buildTaskName() {
        "build${name}"
    }

    abstract File getPlist()

    String getVersionCode() {
        extVersionCode ?: plistParser.getVersionCode(plist) ?: ''
    }

    String getExtVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    String getVersionString() {
        extVersionString ?: plistParser.getVersionString(plist) ?: ''
    }

    String getExtVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    abstract String getBuildableName()

    abstract List<String> buildCmd()

    protected String sdkCmd() {
        conf.sdk.value ? "-sdk ${conf.sdk.value}" : ''
    }
}
