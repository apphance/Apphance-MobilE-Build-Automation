package com.apphance.ameba.plugins.release

import java.io.File
import java.net.URL
import java.util.Collection
import java.util.Locale

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
        return new File(new File(otaDirectory, projectDirectoryName),
        projectConfiguration.fullVersionString)
    }

    URL getVersionedApplicationUrl() {
        return new URL(baseUrl, "${projectDirectoryName}/${projectConfiguration.fullVersionString}/")
    }

    @Override
    public String toString() {
        return this.getProperties()
    }
}
