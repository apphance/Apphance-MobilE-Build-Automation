package com.apphance.ameba
/**
 * Keeps basic configuration of the project.
 */
class ProjectConfiguration {

    String projectName
    File tmpDirectory
    File logDirectory
    File buildDirectory
    String versionString = "NOVERSION"
    Long versionCode = 0

    Collection<String> sourceExcludes = []

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectVersionedName() {
        "${projectName}-${fullVersionString}"
    }

    @Override
    public String toString() {
        this.properties
    }
}
