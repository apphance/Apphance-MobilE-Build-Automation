package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.plugins.release.AmebaArtifact

interface ReleaseConfiguration extends Configuration {

    //TODO how this field is set?
    Collection<String> getReleaseNotes()

    URLProperty getProjectURL()

    String getProjectDirectoryName()

    File getOtaDirectory()

    String getBuildDate()

    AmebaArtifact getSourcesZip()

    AmebaArtifact getDocumentationZip()

    AmebaArtifact getImageMontageFile()

    AmebaArtifact getMailMessageFile()

    AmebaArtifact getQRCodeFile()

    ListStringProperty getReleaseMailFlags()

    String getReleaseMailSubject()

    StringProperty getReleaseMailFrom()

    StringProperty getReleaseMailTo()

    FileProperty getProjectIconFile()

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
