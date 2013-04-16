package com.apphance.ameba.configuration

import com.apphance.ameba.plugins.release.AmebaArtifact

interface ReleaseConfiguration extends Configuration {

    Collection<String> getReleaseNotes()

    URL getBaseURL()

    String getProjectDirectoryName()

    File getOtaDirectory()

    String getBuildDate()

    AmebaArtifact getSourcesZip()

    AmebaArtifact getDocumentationZip()

    AmebaArtifact getImageMontageFile()

    AmebaArtifact getMailMessageFile()

    AmebaArtifact getQRCodeFile()

    Collection<String> getReleaseMailFlags()

    String getReleaseMailSubject()

    String getReleaseMailFrom()

    String getReleaseMailTo()

    File getIconFile()

    AmebaArtifact getGalleryCSS()

    AmebaArtifact getGalleryJS()

    AmebaArtifact getGalleryTrans()

    Locale getLocale()

    File getTargetDirectory()
//    {
//        new File(new File(otaDirectory, projectDirectoryName), projectConfiguration.fullVersionString)
//    }

    URL getVersionedApplicationUrl()
//    {
//        new URL(baseUrl, "${projectDirectoryName}/${projectConfiguration.fullVersionString}/")
//    }
}
