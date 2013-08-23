package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.executor.command.Command
import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import groovy.transform.PackageScope

import static org.gradle.api.logging.Logging.getLogger

class IOSFrameworkArtifactsBuilder extends AbstractIOSArtifactsBuilder<IOSFrameworkArtifactInfo> {

    private logger = getLogger(getClass())

    @Override
    void buildArtifacts(IOSFrameworkArtifactInfo info) {
        prepareFrameworkZip(info)
    }

    @PackageScope
    void prepareFrameworkZip(IOSFrameworkArtifactInfo info) {
        def fa = artifactProvider.framework(info)
        releaseConf.frameworkFiles.put(info.id, fa)
        mkdirs(fa)
        executor.executeCommand(new Command(
                runDir: new File(info.frameworkDir.parent),
                cmd: ['zip', '-r', '-y', fa.location.absolutePath, info.frameworkDir.name]
        ))
        logger.info("Framework zip file created: $fa.location.absolutePath")
    }
}
