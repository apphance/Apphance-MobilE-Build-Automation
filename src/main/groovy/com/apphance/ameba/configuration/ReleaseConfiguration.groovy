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

    void setSourcesZip(AmebaArtifact aa)

    AmebaArtifact getDocumentationZip()

    void setDocumentationZip(AmebaArtifact aa)

    AmebaArtifact getImageMontageFile()

    void setImageMontageFile(AmebaArtifact aa)

    AmebaArtifact getMailMessageFile()

    void setMailMessageFile(AmebaArtifact aa)

    AmebaArtifact getQRCodeFile()

    ListStringProperty getReleaseMailFlags()

    String getReleaseMailSubject()

    StringProperty getReleaseMailFrom()

    StringProperty getReleaseMailTo()

    FileProperty getProjectIconFile()

    AmebaArtifact getGalleryCSS()

    void setGalleryCSS(AmebaArtifact aa)

    AmebaArtifact getGalleryJS()

    void setGalleryJS(AmebaArtifact aa)

    AmebaArtifact getGalleryTrans()

    void setGalleryTrans(AmebaArtifact aa)

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
