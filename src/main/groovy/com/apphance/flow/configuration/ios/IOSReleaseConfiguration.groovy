package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.configuration.ProjectConfiguration.BUILD_DIR
import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.util.file.FileManager.relativeTo
import static groovy.io.FileType.FILES

@Singleton
class IOSReleaseConfiguration extends ReleaseConfiguration {

    Map<String, FlowArtifact> distributionZipFiles = [:]
    Map<String, FlowArtifact> dSYMZipFiles = [:]
    Map<String, FlowArtifact> ipaFiles = [:]
    Map<String, FlowArtifact> manifestFiles = [:]
    Map<String, FlowArtifact> mobileProvisionFiles = [:]
    Map<String, FlowArtifact> ahSYMDirs = [:]
    Map<String, FlowArtifact> dmgImageFiles = [:]

    @Inject IOSVariantsConfiguration iosVariantsConf
    @Inject PlistParser plistParser
    static String ICON_PATTERN = /(?i).*icon.*png/

    @Override
    File defaultIcon() {
        iconFiles.find { it.name.toLowerCase().startsWith('icon') } ?: iconFiles.find()
    }

    @Override
    List<String> possibleIcons() {
        iconFiles.collect { relativeTo(conf.rootDir.absolutePath, it.absolutePath).path }
    }

    private List<File> getIconFiles() {
        def icons = []
        conf.rootDir.traverse(
                type: FILES,
                filter: { File it -> it.name ==~ ICON_PATTERN },
                excludeFilter: ~/.*(${TMP_DIR}|${OTA_DIR}|${BUILD_DIR}).*/) {
            icons << it
        }
        icons
    }

    List<File> findMobileProvisionFiles() {
        def files = []
        conf.rootDir.traverse(
                type: FILES,
                nameFilter: ~/.*\.mobileprovision/,
                excludeFilter: ~/.*(${TMP_DIR}|${OTA_DIR}|${BUILD_DIR}).*/) {
            files << it
        }
        files
    }

    @Override
    boolean canBeEnabled() {
        !findMobileProvisionFiles().empty
    }

    @Override
    String getMessage() {
        "To enable configuration you need to provide mobile provision file somewhere in project directory.\n" +
                "File must match *.mobileprovision. Can be placed anywhere in project source."
    }
}
