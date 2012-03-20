package com.apphance.ameba

import java.io.File
import java.net.URL
import java.util.Collection
import java.util.Locale

/**
 * Keeps basic configuration about the project.
 */
class ProjectConfiguration  {
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
