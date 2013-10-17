package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.*
import static groovy.io.FileType.FILES
import static java.text.MessageFormat.format

@Singleton
@Mixin(FlowUtils)
class IOSReleaseConfiguration extends ReleaseConfiguration {

    Map<String, FlowArtifact> distributionZipFiles = [:]
    Map<String, FlowArtifact> xcArchiveZipFiles = [:]
    Map<String, FlowArtifact> dSYMZipFiles = [:]
    Map<String, FlowArtifact> ipaFiles = [:]
    Map<String, FlowArtifact> manifestFiles = [:]
    Map<String, FlowArtifact> mobileProvisionFiles = [:]
    Map<String, FlowArtifact> ahSYMDirs = [:]
    Map<String, FlowArtifact> dmgImageFiles = [:]
    Map<String, FlowArtifact> frameworkFiles = [:]

    @Inject IOSVariantsConfiguration iosVariantsConf
    @Inject PlistParser plistParser
    static final String ICON_PATTERN = /(?i).*icon.*png/

    File defaultIcon = copy('/com/apphance/flow/configuration/ios/ios-icon.svg', new File(temporaryDir, 'ios-icon.svg'))

    @Override
    File possibleIcon() {
        def icon = iconFiles.find { it.name.toLowerCase().startsWith('icon') } ?: iconFiles.find()
        icon ? relativeTo(conf.rootDir.absolutePath, icon.absolutePath) : null
    }

    @Override
    List<String> possibleIcons() {
        iconFiles.collect { relativeTo(conf.rootDir.absolutePath, it.absolutePath).path }
    }

    private List<File> getIconFiles() {
        def icons = []
        conf.rootDir.traverse(
                type: FILES,
                maxDepth: MAX_RECURSION_LEVEL,
                filter: { File it -> it.name ==~ ICON_PATTERN },
                excludeFilter: EXCLUDE_FILTER) {
            icons << it
        }
        icons
    }

    @Lazy
    List<File> mobileprovisionFiles = {
        def files = []
        conf.rootDir.traverse(
                type: FILES,
                maxDepth: MAX_RECURSION_LEVEL,
                nameFilter: ~/.*\.mobileprovision/,
                excludeFilter: EXCLUDE_FILTER) {
            files << it
        }
        files
    }()

    @Override
    boolean canBeEnabled() {
        !mobileprovisionFiles.empty
    }

    @Override
    String explainDisabled() {
        format(validationBundle.getString('disabled.conf.ios.release'), configurationName)
    }
}
