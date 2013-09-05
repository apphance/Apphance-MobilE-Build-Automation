package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.variants.AbstractVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static com.apphance.flow.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ
import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.ARCHIVE_ACTION
import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.LAUNCH_ACTION
import static com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask.WHITESPACE_PATTERN
import static com.apphance.flow.util.file.FileManager.relativeTo
import static com.google.common.base.Preconditions.checkArgument
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.apache.commons.lang.StringUtils.isNotEmpty

class IOSVariant extends AbstractVariant {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject PlistParser plistParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSExecutor executor
    @Inject XCSchemeParser schemeParser
    @Inject IOSSchemeInfo schemeInfo

    private bundle = getBundle('validation')

    @Inject
    IOSVariant(@Assisted String name) {
        super(name)
    }

    @Override
    @Inject
    void init() {

        mode.name = "ios.variant.${name}.mode"
        mobileprovision.name = "ios.variant.${name}.mobileprovision"

        frameworkName.name = "ios.variant.${name}.framework.name"
        frameworkHeaders.name = "ios.variant.${name}.framework.headers"
        frameworkResources.name = "ios.variant.${name}.framework.resources"

        super.init()
    }

    final String prefix = 'ios'

    @PackageScope
    IOSConfiguration getConf() {
        super.@conf as IOSConfiguration
    }

    def mode = new IOSBuildModeProperty(
            message: "Build mode for the variant",
            required: { true },
            possibleValues: { possibleBuildModes },
            validator: { it in possibleBuildModes }
    )

    @Lazy
    @PackageScope
    List<String> possibleBuildModes = {
        if (schemeInfo.schemeBuildable(schemeFile) && schemeInfo.schemeHasSingleBuildableTarget(schemeFile))
            return [DEVICE, SIMULATOR]*.name()
        else if (!schemeInfo.schemeBuildable(schemeFile))
            return [FRAMEWORK]*.name()
        []
    }()

    private FileProperty mobileprovision = new FileProperty(
            message: "Mobile provision file for variant defined",
            interactive: { mobileprovisionEnabled },
            required: { mobileprovisionEnabled },
            possibleValues: { possibleMobileProvisionFiles()*.path as List<String> },
            validator: { it in (possibleMobileProvisionFiles()*.path as List<String>) }
    )

    @Lazy
    @PackageScope
    boolean mobileprovisionEnabled = {
        def enabled = releaseConf.enabled && mode.value == DEVICE
        if (!enabled)
            this.@mobileprovision.resetValue()
        enabled
    }()

    FileProperty getMobileprovision() {
        new FileProperty(value: new File(tmpDir, this.@mobileprovision.value.path))
    }

    @PackageScope
    List<File> possibleMobileProvisionFiles() {
        def mp = releaseConf.findMobileProvisionFiles()
        mp ? mp.collect { relativeTo(conf.rootDir.absolutePath, it.absolutePath) } : []
    }

    def frameworkName = new StringProperty(
            message: 'Framework name',
            required: { mode.value == FRAMEWORK },
            interactive: { mode.value == FRAMEWORK },
            validator: { isNotEmpty(it) }
    )
    def frameworkHeaders = new ListStringProperty(interactive: { false }, required: { false })
    def frameworkResources = new ListStringProperty(interactive: { false }, required: { false })

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
        mode.value == DEVICE
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

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectName() {
        if (mode.value in [SIMULATOR, DEVICE]) {
            String bundleDisplayName = plistParser.bundleDisplayName(plist)
            checkArgument(isNotBlank(bundleDisplayName),
                    """|Cant find 'CFBundleDisplayName' property in file $plist.absolutePath
                   |Is project configured well?""".stripMargin())
            return plistParser.evaluate(bundleDisplayName, target, buildConfiguration)
        } else if (mode.value == FRAMEWORK) {
            return frameworkName.value
        }
        return name
    }

    @Lazy
    File pbxFile = {
        def pbx = new File("$tmpDir.absolutePath/$conf.xcodeDir.value", PROJECT_PBXPROJ)
        pbx.exists() ? pbx : new File("${conf.rootDir}/${conf.xcodeDir.value}", PROJECT_PBXPROJ)
    }()

    File getPlist() {
        String confName = schemeParser.configuration(schemeFile, LAUNCH_ACTION)
        String blueprintId = schemeParser.blueprintIdentifier(schemeFile)
        new File(tmpDir, pbxJsonParser.plistForScheme(pbxFile, confName, blueprintId))
    }

    String getTarget() {
        pbxJsonParser.targetForBlueprintId.call(pbxFile, schemeParser.blueprintIdentifier(schemeFile))
    }

    String getBuildConfiguration() {
        schemeParser.configuration(schemeFile, LAUNCH_ACTION)
    }

    String getArchiveConfiguration() {
        schemeParser.configuration(schemeFile, ARCHIVE_ACTION)
    }

    String getArchiveTaskName() {
        "archive$name".replaceAll('\\s', '')
    }

    String getFrameworkTaskName() {
        "framework$name".replaceAll('\\s', '')
    }

    File getSchemeFile() {
        def filename = "xcshareddata/xcschemes/${name}.xcscheme"
        def tmpScheme = new File("$tmpDir/$conf.xcodeDir.value", filename)
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

        if (mode.value == FRAMEWORK) {
            defaultValidation frameworkName
            check(frameworkHeaders.value.every { new File(conf.rootDir, it).exists() }, bundle.getString('exception.ios.framework.invalid.headers'))
            check(frameworkResources.value.every { new File(conf.rootDir, it).exists() }, bundle.getString('exception.ios.framework.invalid.resources'))
        }
    }
}