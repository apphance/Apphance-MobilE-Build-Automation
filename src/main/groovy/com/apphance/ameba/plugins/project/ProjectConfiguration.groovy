package com.apphance.ameba.plugins.project
/**
 * Keeps basic configuration of the project.
 */
class ProjectConfiguration {

    String projectName
    File tmpDirectory
    File logDirectory
    File buildDirectory
    String versionString = 'NOVERSION'
    Long versionCode = 0

    Collection<String> sourceExcludes = []

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    String getProjectVersionedName() {
        "$projectName-$fullVersionString"
    }

    void updateVersionDetails(Map versionDetails) {
        versionString = versionDetails.versionString
        versionCode = versionDetails.versionCode
    }

    @Override
    public String toString() {
        this.properties
    }
}
