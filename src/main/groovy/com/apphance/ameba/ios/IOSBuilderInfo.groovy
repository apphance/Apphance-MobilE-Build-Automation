package com.apphance.ameba.ios
/**
 * Information for single artict being built. Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class IOSBuilderInfo {
    String id
    String target
    String configuration
    File buildDirectory
    String fullReleaseName
    String filePrefix
    File mobileprovisionFile
    File plistFile

    @Override
    public String toString() {
        return this.getProperties()
    }
}
