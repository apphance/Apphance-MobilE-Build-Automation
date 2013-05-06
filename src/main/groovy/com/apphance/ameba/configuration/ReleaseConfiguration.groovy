package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.plugins.release.AmebaArtifact

interface ReleaseConfiguration extends Configuration {

    Collection<String> getReleaseNotes()

    URLProperty getProjectURL()

    String getProjectDirName()

    File getOtaDir()

    String getBuildDate()

    AmebaArtifact getSourcesZip()

    void setSourcesZip(AmebaArtifact aa)

    AmebaArtifact getImageMontageFile()

    void setImageMontageFile(AmebaArtifact aa)

    AmebaArtifact getMailMessageFile()

    void setMailMessageFile(AmebaArtifact aa)

    AmebaArtifact getQRCodeFile()

    ListStringProperty getReleaseMailFlags()

    String getReleaseMailSubject()

    StringProperty getReleaseMailFrom()

    StringProperty getReleaseMailTo()

    FileProperty getIconFile()

    AmebaArtifact getGalleryCSS()

    void setGalleryCSS(AmebaArtifact aa)

    AmebaArtifact getGalleryJS()

    void setGalleryJS(AmebaArtifact aa)

    AmebaArtifact getGalleryTrans()

    void setGalleryTrans(AmebaArtifact aa)

    Locale getLocale()

    File getTargetDirectory()

    URL getVersionedApplicationUrl()

    URL getBaseURL()

    String getMailPort()

    String getMailServer()
}
