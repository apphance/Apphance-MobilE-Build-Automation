package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser

import static com.apphance.ameba.configuration.apphance.ApphanceMode.DISABLED

abstract class AbstractIOSVariant extends AbstractConfiguration {

    final String name
    IOSConfiguration conf
    IOSReleaseConfiguration releaseConf
    ApphanceConfiguration apphanceConf
    PlistParser plistParser
    PbxJsonParser pbxJsonParser
    PropertyReader reader

    AbstractIOSVariant(String name,
                       IOSConfiguration conf,
                       IOSReleaseConfiguration releaseConf,
                       ApphanceConfiguration apphanceConf,
                       PbxJsonParser pbxJsonParser,
                       PlistParser plistParser,
                       PropertyPersister persister) {
        this.name = name
        this.conf = conf
        this.releaseConf = releaseConf
        this.apphanceConf = apphanceConf
        this.plistParser = plistParser
        this.pbxJsonParser = pbxJsonParser
        this.propertyPersister = persister

        initFields()
    }

    private void initFields() {
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
            possibleValues: { [] as List<String> }, //TODO
            validator: { true } //TODO
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
}
