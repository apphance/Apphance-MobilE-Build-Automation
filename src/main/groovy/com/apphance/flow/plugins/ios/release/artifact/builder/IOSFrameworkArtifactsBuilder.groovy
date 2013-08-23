package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import groovy.transform.PackageScope

class IOSFrameworkArtifactsBuilder extends AbstractIOSArtifactsBuilder<IOSFrameworkArtifactInfo> {

    @Override
    void buildArtifacts(IOSFrameworkArtifactInfo info) {
        prepareFrameworkZip(info)
    }

    @PackageScope
    void prepareFrameworkZip(IOSFrameworkArtifactInfo info) {

    }
}
