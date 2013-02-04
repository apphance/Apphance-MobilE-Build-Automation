package com.apphance.ameba.plugins.release

import com.apphance.ameba.ProjectConfiguration

/**
 * Configuration for project release.
 *
 */
class ProjectReleaseConfiguration {

    ProjectConfiguration projectConfiguration
    Collection<String> releaseNotes
    URL baseUrl = new URL("http://example.com")
    String projectDirectoryName

    File otaDirectory
    String buildDate

    AmebaArtifact sourcesZip
    AmebaArtifact documentationZip
    AmebaArtifact imageMontageFile
    AmebaArtifact mailMessageFile
    AmebaArtifact qrCodeFile

    Collection<String> releaseMailFlags
    String releaseMailSubject
    String releaseMailFrom
    String releaseMailTo

    File iconFile

    AmebaArtifact galleryCss
    AmebaArtifact galleryJs
    AmebaArtifact galleryTrans

    Locale locale

    File getTargetDirectory() {
        new File(new File(otaDirectory, projectDirectoryName), projectConfiguration.fullVersionString)
    }

    URL getVersionedApplicationUrl() {
        new URL(baseUrl, "${projectDirectoryName}/${projectConfiguration.fullVersionString}/")
    }

    @Override
    public String toString() {
        this.properties
    }
}
