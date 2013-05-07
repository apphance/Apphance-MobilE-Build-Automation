package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.plugins.release.AmebaArtifact

interface ReleaseConfiguration extends Configuration {

    def MAIL_PATTERN = /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/
    def ALL_EMAIL_FLAGS = [
            'installableSimulator',
            'qrCode',
            'imageMontage'
    ]

    Collection<String> getReleaseNotes()

    URLProperty getProjectURL()

    String getProjectDirName()

    File getOtaDir()

    String getBuildDate()

    AmebaArtifact getOtaIndexFile()

    void setOtaIndexFile(AmebaArtifact aa)

    AmebaArtifact getFileIndexFile()

    void setFileIndexFile(AmebaArtifact aa)

    AmebaArtifact getPlainFileIndexFile()

    void setPlainFileIndexFile(AmebaArtifact aa)

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
