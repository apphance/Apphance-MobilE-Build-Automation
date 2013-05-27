package com.apphance.ameba.plugins.ios.builder

import com.apphance.ameba.configuration.ios.IOSBuildMode
import org.gradle.api.Project

/**
 * Information for single artifact being built. Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class IOSBuilderInfo {

    String id
    String target
    String configuration
    IOSBuildMode mode
    File buildDir
    String fullReleaseName
    String filePrefix
    File mobileprovision
    File plist

    @Override
    public String toString() {
        this.properties
    }
}
