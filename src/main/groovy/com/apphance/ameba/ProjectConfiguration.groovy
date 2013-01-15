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
    Collection<String> commitFilesOnVCS = []

    String getFullVersionString() {
        return "${versionString}_${versionCode}"
    }

    String getProjectVersionedName() {
        return "${projectName}-${fullVersionString}"
    }

    @Override
    public String toString() {
        return this.getProperties()
    }
}
