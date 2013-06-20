package com.apphance.flow.plugins.ios.builder

import com.apphance.flow.configuration.ios.IOSBuildMode

/**
 * Information for single artifact being built. Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class IOSBuilderInfo {

    String id
    String target
    String configuration
    String buildableName
    IOSBuildMode mode
    File buildDir
    String fullReleaseName
    String filePrefix
    String versionString
    File mobileprovision
    File plist


    @Override
    public String toString() {
        this.properties
    }
}
