package com.apphance.ameba.configuration.ios.variants

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.ios.IOSBuildMode
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.IOSBuildModeProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.configuration.variants.AbstractVariant
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.parsers.PbxJsonParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.ameba.configuration.ios.IOSBuildMode.SIMULATOR

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
    @Inject
    IOSExecutor executor

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

        mode.name = "ios.variant.${name}.mode"
        mode.message = "Build mode for the variant, it describes the environment the artifact is built for: (DEVICE|SIMULATOR)"

        super.init()
    }

    def mobileprovision = new FileProperty(
            interactive: { releaseConf.enabled },
            required: { releaseConf.enabled },
            possibleValues: { releaseConf.findMobileProvisionFiles()*.name as List<String> },
            validator: { it in releaseConf.findMobileProvisionFiles()*.name }
    )

    def mode = new IOSBuildModeProperty(
            required: { true },
            defaultValue: { (configuration.contains('debug') || configuration.contains('dev')) ? SIMULATOR : DEVICE },
            possibleValues: { possibleBuildModeValues() },
            validator: { it in possibleBuildModeValues() }
    )

    @PackageScope
    List<String> possibleBuildModeValues() {
        IOSBuildMode.values()*.name() as List<String>
    }

    @Override
    String getConfigurationName() {
        "iOS Variant ${name}"
    }

    String getVersionCode() {
        conf.extVersionCode ?: plistParser.evaluate(plistParser.versionCode(plist), target, configuration) ?: ''
    }

    String getVersionString() {
        conf.extVersionString ?: plistParser.evaluate(plistParser.versionString(plist), target, configuration) ?: ''
    }

    protected String sdkCmd() {
        switch (mode.value) {
            case SIMULATOR:
                println "sim"
                conf.simulatorSdk.value ? "-sdk ${conf.simulatorSdk.value}" : ''
                break
            case DEVICE:
                println "dev"
                conf.sdk.value ? "-sdk ${conf.sdk.value}" : ''
                break
            default:
                ''
        }
    }

    protected String buildModeCmd() {
        mode.value == SIMULATOR ? '-arch i386' : ''
    }

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectName() {
        String bundleDisplayName = plistParser.bundleDisplayName(plist)
        plistParser.evaluate(bundleDisplayName, target, configuration)
    }

    String getBuildableName() {
        executor.buildSettings(target, configuration)['FULL_PRODUCT_NAME']
    }

    abstract File getPlist()

    abstract String getConfiguration()

    abstract String getTarget()

    abstract List<String> buildCmd()
}
