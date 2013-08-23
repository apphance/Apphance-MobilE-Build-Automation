package com.apphance.flow.plugins.ios.release.artifact

import com.apphance.flow.configuration.ios.IOSBuildMode

class IOSArtifactInfo {

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
