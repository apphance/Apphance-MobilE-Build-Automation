package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.configuration.variants.AbstractVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ
import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.ARCHIVE_ACTION
import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.LAUNCH_ACTION
import static com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask.WHITESPACE_PATTERN
import static com.apphance.flow.util.file.FileManager.relativeTo
import static com.google.common.base.Preconditions.checkArgument
import static java.io.File.separator
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
            defaultValue: { (buildConfiguration.contains('debug') || buildConfiguration.contains('dev')) ? SIMULATOR : DEVICE },
            possibleValues: { possibleBuildModeValues },
            validator: { it in possibleBuildModeValues }
    )

    @Lazy
    @PackageScope
    List<String> possibleBuildModeValues = {
        IOSBuildMode.values()*.name() as List<String>
    }()

    String getBundleId() {
        plistParser.evaluate(plistParser.bundleId(plist), target, buildConfiguration) ?: ''
    }

    @Override
    List<String> possibleApphanceLibVersions() {
        apphanceArtifactory.iOSLibraries(apphanceMode.value, apphanceDependencyArch)
    }

    @Lazy
    @PackageScope
    String apphanceDependencyArch = {
        def af = apphanceArtifactory.iOSArchs(apphanceMode.value)
        af.retainAll(availableXCodeArchitectures)
        af.unique().sort()[-1]
    }()

    @Lazy
    @PackageScope
    Collection<String> availableXCodeArchitectures = {
        (executor.buildSettings(target, buildConfiguration)['ARCHS'].split(' ').toList() +
                executor.buildSettings(target, archiveConfiguration)['ARCHS'].split(' ').toList())*.trim()
    }()

    @Lazy
    boolean apphanceEnabledForVariant = {
        mode.value != SIMULATOR
    }()

    @Override
    String getConfigurationName() {
        "iOS Variant ${name}"
    }

    String getVersionCode() {
        conf.extVersionCode ?: plistParser.evaluate(plistParser.bundleVersion(plist), target, buildConfiguration) ?: ''
    }

    String getVersionString() {
        conf.extVersionString ?: plistParser.evaluate(plistParser.bundleShortVersionString(plist), target, buildConfiguration) ?: ''
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

    List<String> getArchCmd() {
        mode.value == SIMULATOR ? ['-arch', 'i386'] : []
    }

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectName() {
        String bundleDisplayName = plistParser.bundleDisplayName(plist)
        checkArgument(isNotBlank(bundleDisplayName),
                """|Cant find 'CFBundleDisplayName' property in file $plist.absolutePath
                   |Is project configured well?""".stripMargin())
        plistParser.evaluate(bundleDisplayName, target, buildConfiguration)
    }

    @Lazy
    File variantPbx = {
        def pbx = new File("$tmpDir.absolutePath/$conf.xcodeDir.value", PROJECT_PBXPROJ)
        pbx.exists() ? pbx : new File("${conf.rootDir}/${conf.xcodeDir.value}", PROJECT_PBXPROJ)
    }()

    File getPlist() {
        String confName = schemeParser.configuration(schemeFile, LAUNCH_ACTION)
        String blueprintId = schemeParser.blueprintIdentifier(schemeFile)
        new File(tmpDir, pbxJsonParser.plistForScheme(variantPbx, confName, blueprintId))
    }

    String getTarget() {
        pbxJsonParser.targetForBlueprintId(variantPbx, schemeParser.blueprintIdentifier(schemeFile))
    }

    String getBuildConfiguration() {
        schemeParser.configuration(schemeFile, LAUNCH_ACTION)
    }

    String getArchiveConfiguration() {
        schemeParser.configuration(schemeFile, ARCHIVE_ACTION)
    }

    List<String> getBuildCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', name] + sdkCmd + archCmd + ['clean', 'build']
    }

    String getArchiveTaskName() {
        "archive$name".replaceAll('\\s', '')
    }

    List<String> getArchiveCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', name] + sdkCmd + archCmd + ['clean', 'archive']
    }

    File getSchemeFile() {
        def filename = "xcshareddata${separator}xcschemes$separator${name}.xcscheme"
        def tmpScheme = new File("$tmpDir$separator$conf.xcodeDir.value", filename)
        tmpScheme?.exists() ? tmpScheme : new File(conf.xcodeDir.value, filename)
    }

    @Override
    void checkProperties() {
        super.checkProperties()
        def ec = conf.extVersionCode
        if (ec)
            check ec.matches('[0-9]+'), bundle.getString('exception.ios.version.code.ext')
        def es = conf.extVersionString
        if (es)
            check((isNotEmpty(es) && !WHITESPACE_PATTERN.matcher(es).find()), bundle.getString('exception.ios.version.string.ext'))
    }
}

