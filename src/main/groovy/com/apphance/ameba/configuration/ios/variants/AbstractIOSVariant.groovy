package com.apphance.ameba.configuration.ios.variants

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.configuration.variants.AbstractVariant
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

abstract class AbstractIOSVariant extends AbstractVariant {

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

    final String prefix = 'ios'

    @Inject
    AbstractIOSVariant(@Assisted String name) {
        super(name)
    }

    @Override
    @Inject
    void init() {

        mobileprovision.name = "ios.variant.${name}.mobileprovision"
        mobileprovision.message = "Mobile provision file for '$name'"

        super.init()
    }

    def mobileprovision = new FileProperty(
            interactive: { releaseConf.enabled },
            required: { releaseConf.enabled },
            possibleValues: { releaseConf.findMobileProvisionFiles()*.name as List<String> },
            validator: { it in releaseConf.findMobileProvisionFiles()*.name }
    )

    @Override
    String getConfigurationName() {
        "iOS Variant ${name}"
    }

    String getVersionCode() {
        extVersionCode ?: plistParser.versionCode(plist) ?: ''
    }

    String getExtVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    String getVersionString() {
        extVersionString ?: plistParser.versionString(plist) ?: ''
    }

    String getExtVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    protected String sdkCmd() {
        conf.sdk.value ? "-sdk ${conf.sdk.value}" : ''
    }

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectName() {
        //TODO this value should be taken from plist - CFBundleDisplayName
        //TODO the value of the mentioned key may refer to pbxjproj file
        null
    }



    abstract File getPlist()

    abstract String getBuildableName()

    abstract List<String> buildCmd()

    abstract String getConfiguration()

    abstract String getTarget()
}
