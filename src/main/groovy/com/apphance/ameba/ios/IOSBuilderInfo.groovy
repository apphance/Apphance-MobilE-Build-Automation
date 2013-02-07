package com.apphance.ameba.ios
/**
 * Information for single artifact being built. Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class IOSBuilderInfo {

    String id
    String target
    String configuration
    File buildDirectory
    String fullReleaseName
    String filePrefix
    File mobileProvisionFile
    File plistFile

    @Override
    public String toString() {
        this.properties
    }
}
