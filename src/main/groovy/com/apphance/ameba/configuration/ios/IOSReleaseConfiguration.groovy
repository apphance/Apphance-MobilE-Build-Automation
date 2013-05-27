package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.relativeTo
import static groovy.io.FileType.FILES

@com.google.inject.Singleton
class IOSReleaseConfiguration extends ReleaseConfiguration {

    Map<String, AmebaArtifact> distributionZipFiles = [:]
    Map<String, AmebaArtifact> dSYMZipFiles = [:]
    Map<String, AmebaArtifact> ipaFiles = [:]
    Map<String, AmebaArtifact> manifestFiles = [:]
    Map<String, AmebaArtifact> mobileProvisionFiles = [:]
    Map<String, AmebaArtifact> ahSYMDirs = [:]
    Map<String, AmebaArtifact> dmgImageFiles = [:]

    @Inject IOSVariantsConfiguration iosVariantsConf
    @Inject PlistParser plistParser
    static String ICON_PATTERN = /(?i).*icon.*png/

    @Override
    File defaultIcon() {
        iconFiles.find { it.name.startsWith('Icon') } ?: iconFiles.find()
    }

    @Override
    List<String> possibleIcons() {
        iconFiles.collect { relativeTo(conf.rootDir.absolutePath, it.absolutePath).path }
    }

    private List<File> getIconFiles() {
        def icons = []
        conf.rootDir.traverse(type: FILES, filter: {File it -> it.name ==~ ICON_PATTERN}) {
            icons << it
        }
        icons
    }

    List<File> findMobileProvisionFiles() {
        def files = []
        conf.rootDir.eachFileRecurse(FILES) {
            if (it.name.endsWith('.mobileprovision')) {
                files << it
            }
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
