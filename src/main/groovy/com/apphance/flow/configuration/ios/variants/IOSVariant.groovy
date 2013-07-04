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
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ProjectConfiguration.BUILD_DIR
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ
import static com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask.WHITESPACE_PATTERN
import static com.apphance.flow.util.file.FileManager.relativeTo
import static com.google.common.base.Preconditions.checkArgument
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

class IOSVariant extends AbstractVariant {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject PlistParser plistParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject PropertyReader reader
    @Inject IOSExecutor executor
    @Inject XCSchemeParser schemeParser

    private bundle = getBundle('validation')

    @Inject
    IOSVariant(@Assisted String name) {
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
    )

    String getEffectiveBundleId() {
        bundleId.value ?: plistParser.evaluate(plistParser.bundleId(plist), target, configuration) ?: ''
    }

    @Override
    List<String> possibleApphanceLibVersions() {
        apphanceArtifactory.iOSLibraries(apphanceMode.value, apphanceDependencyArch())
    }

    @PackageScope
    String apphanceDependencyArch() {
        def xc = availableXCodeArchitectures()
        def af = apphanceArtifactory.iOSArchs(apphanceMode.value)
        af.retainAll(xc)
        af.unique().sort()[-1]
    }

    @PackageScope
    Collection<String> availableXCodeArchitectures() {
        executor.buildSettings(target, configuration)['ARCHS'].split(' ')*.trim()
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

    @Lazy
    File variantPbx = {
        def pbx = new File("$tmpDir.absolutePath/$conf.xcodeDir.value", PROJECT_PBXPROJ)
        pbx.exists() ? pbx : new File("${conf.rootDir}/${conf.xcodeDir.value}", PROJECT_PBXPROJ)
    }()

    File getPlist() {
        String confName = schemeParser.configurationName(name)
        String blueprintId = schemeParser.blueprintIdentifier(name)
        new File(tmpDir, pbxJsonParser.plistForScheme(variantPbx, confName, blueprintId))
    }

    String getTarget() {
        pbxJsonParser.targetForBlueprintId(variantPbx, schemeParser.blueprintIdentifier(name))
    }

    String getConfiguration() {
        schemeParser.configurationName(name)
    }

    List<String> getBuildCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', name] + sdkCmd + archCmd + [buildDirCmd]
    }

    String getArchiveTaskName() {
        "archive$name".replaceAll('\\s', '')
    }

    List<String> archiveCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', name] + sdkCmd + archCmd + [buildDirCmd] + ['archive']
    }

    @Override
    void checkProperties() {
        super.checkProperties()
        check versionCode.matches('[0-9]+'), format(bundle.getString('exception.ios.version.code'), plist.absolutePath)
        check((isNotEmpty(versionString) && !WHITESPACE_PATTERN.matcher(versionString).find()),
                format(bundle.getString('exception.ios.version.string'), plist.absolutePath))
    }
}
