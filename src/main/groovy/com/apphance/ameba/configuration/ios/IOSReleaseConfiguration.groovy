package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.plugins.release.AmebaArtifact

class IOSReleaseConfiguration extends AbstractConfiguration implements ReleaseConfiguration {

    String configurationName = 'iOS Release Configuration'

    @Override
    Collection<String> getReleaseNotes() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    URLProperty getProjectURL() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getProjectDirName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getOtaDir() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getBuildDate() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getSourcesZip() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setSourcesZip(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getDocumentationZip() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setDocumentationZip(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getImageMontageFile() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setImageMontageFile(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getMailMessageFile() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setMailMessageFile(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getQRCodeFile() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    ListStringProperty getReleaseMailFlags() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getReleaseMailSubject() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    StringProperty getReleaseMailFrom() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    StringProperty getReleaseMailTo() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    FileProperty getProjectIconFile() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getGalleryCSS() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setGalleryCSS(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getGalleryJS() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setGalleryJS(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    AmebaArtifact getGalleryTrans() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    void setGalleryTrans(AmebaArtifact aa) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Locale getLocale() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getTargetDirectory() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    URL getVersionedApplicationUrl() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean isEnabled() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    URL getBaseURL() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }
}
