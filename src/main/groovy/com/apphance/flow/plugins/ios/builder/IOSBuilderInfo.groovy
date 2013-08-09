package com.apphance.flow.plugins.ios.builder

import com.apphance.flow.configuration.ios.IOSBuildMode

/**
 * Information for single artifact being built. Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class IOSBuilderInfo {

    String id
    String appName
    String productName
    IOSBuildMode mode
    File archiveDir
    String filePrefix
    String versionString
    File mobileprovision

    @Override
    public String toString() {
        this.properties
    }
}
