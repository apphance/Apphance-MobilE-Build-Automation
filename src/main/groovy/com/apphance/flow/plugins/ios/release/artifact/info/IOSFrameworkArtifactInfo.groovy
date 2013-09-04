package com.apphance.flow.plugins.ios.release.artifact.info

import com.apphance.flow.util.FlowUtils

class IOSFrameworkArtifactInfo extends AbstractIOSArtifactInfo {

    protected FlowUtils fu = new FlowUtils()

    String frameworkName
    File simLib
    File deviceLib
    List<String> headers
    List<String> resources

    @Lazy File frameworkDir = { new File(fu.temporaryDir, "${frameworkName}.framework") }()
    @Lazy File versionsDir = { new File(frameworkDir, 'Versions/A') }()
    @Lazy File resourcesDir = { new File(versionsDir, 'Resources') }()
    @Lazy File headersDir = { new File(versionsDir, 'Headers') }()
}
