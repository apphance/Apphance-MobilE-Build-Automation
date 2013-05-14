package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.io.FileType

@com.google.inject.Singleton
class IOSReleaseConfiguration extends ReleaseConfiguration {

    Map<String, AmebaArtifact> distributionZipFiles = [:]
    Map<String, AmebaArtifact> dSYMZipFiles = [:]
    Map<String, AmebaArtifact> ipaFiles = [:]
    Map<String, AmebaArtifact> manifestFiles = [:]
    Map<String, AmebaArtifact> mobileProvisionFiles = [:]
    Map<String, AmebaArtifact> ahSYMDirs = [:]
    Map<String, AmebaArtifact> dmgImageFiles = [:]

    @Override
    File defaultIcon() {
        throw new UnsupportedOperationException('not implemented yet')
    }

    @Override
    List<String> possibleIcons() {
        throw new UnsupportedOperationException('not implemented yet')
    }

    List<File> findMobileProvisionFiles() {
        def files = []
        conf().rootDir.eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.mobileprovision')) {
                files << it
            }
        }
        files
    }

    private IOSConfiguration conf() {
        super.conf as IOSConfiguration
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
