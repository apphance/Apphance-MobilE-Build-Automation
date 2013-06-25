package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.configuration.variants.AbstractVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ProjectConfiguration.BUILD_DIR
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask.WHITESPACE_PATTERN
import static com.apphance.flow.util.file.FileManager.relativeTo
import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

abstract class AbstractIOSVariant extends AbstractVariant {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject PlistParser plistParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject PropertyReader reader
    @Inject IOSExecutor executor

    @Inject
    AbstractIOSVariant(@Assisted String name) {
        super(name)
    }

    @Override
    @Inject
    void init() {

        mobileprovision.name = "ios.variant.${name}.mobileprovision"
        mode.name = "ios.variant.${name}.mode"
        bundleId.name = "ios.variant.${name}.bundleId"

        super.init()
    }

    final String prefix = 'ios'

    @PackageScope
    IOSConfiguration getConf() {
        super.@conf as IOSConfiguration
    }

    private FileProperty mobileprovision = new FileProperty(
            message: "Mobile provision file for variant defined",
            interactive: { releaseConf.enabled },
            required: { releaseConf.enabled },
            possibleValues: { possibleMobileProvisionFiles()*.path as List<String> },
            validator: { it in (possibleMobileProvisionFiles()*.path as List<String>) }
    )

    FileProperty getMobileprovision() {
        new FileProperty(value: new File(tmpDir, this.@mobileprovision.value.path))
    }

    @PackageScope
    List<File> possibleMobileProvisionFiles() {
        def mp = releaseConf.findMobileProvisionFiles()
        mp ? mp.collect { relativeTo(conf.rootDir.absolutePath, it.absolutePath) } : []
    }

    def mode = new IOSBuildModeProperty(
            message: "Build mode for the variant, it describes the environment the artifact is built for: (DEVICE|SIMULATOR)",
            required: { true },
            defaultValue: { (configuration.contains('debug') || configuration.contains('dev')) ? SIMULATOR : DEVICE },
            possibleValues: { possibleBuildModeValues() },
            validator: { it in possibleBuildModeValues() }
    )

    @PackageScope
    List<String> possibleBuildModeValues() {
        IOSBuildMode.values()*.name() as List<String>
    }

    def bundleId = new StringProperty(
            message: "Bundle ID for variant defined. If present will be replaced during build process",
            //TODO validator (domain name?)
    )

    String getEffectiveBundleId() {
        bundleId.value ?: plistParser.evaluate(plistParser.bundleId(plist), target, configuration) ?: ''
    }

    @Lazy
    boolean apphanceEnabledForVariant = {
        mode.value != SIMULATOR
    }()

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

    protected List<String> getSdkCmd() {
        switch (mode.value) {
            case SIMULATOR:
                conf.simulatorSdk.value ? ['-sdk', conf.simulatorSdk.value] : []
                break
            case DEVICE:
                conf.sdk.value ? ['-sdk', conf.sdk.value] : []
                break
            default:
                []
        }
    }

    protected List<String> getArchCmd() {
        mode.value == SIMULATOR ? ['-arch', 'i386'] : []
    }

    protected String getBuildDirCmd() {
        "CONFIGURATION_BUILD_DIR=$buildDir.absolutePath"
    }

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectName() {
        String bundleDisplayName = plistParser.bundleDisplayName(plist)
        checkArgument(isNotBlank(bundleDisplayName),
                """|Cant find 'CFBundleDisplayName' property in file $plist.absolutePath
                   |Is project configured well?""".stripMargin())
        plistParser.evaluate(bundleDisplayName, target, configuration)
    }

    String getBuildableName() {
        executor.buildSettings(target, configuration)['FULL_PRODUCT_NAME']
    }

    File getBuildDir() {
        new File(tmpDir, BUILD_DIR)
    }

    abstract File getPlist()

    abstract String getConfiguration()

    abstract String getTarget()

    abstract List<String> buildCmd()

    @Override
    void checkProperties() {
        super.checkProperties()
        check versionCode.matches('[0-9]+'), """|Property 'versionCode' must have numerical value! Check 'version.code'
                                                |system property or 'VERSION_CODE' env variable
                                                |or $plist.absolutePath file!""".stripMargin()
        check((isNotEmpty(versionString) && !WHITESPACE_PATTERN.matcher(versionString).find()), """|Property 'versionString' must not have
                                                                    |whitespace characters! Check 'version.string'
                                                                    |system property or 'VERSION_STRING' env
                                                                    |variable or $plist.absolutePath file!""".stripMargin())
    }
}
