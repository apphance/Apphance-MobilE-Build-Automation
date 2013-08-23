package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.release.artifact.AbstractIOSArtifactInfo
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.transform.PackageScope

import javax.inject.Inject

abstract class AbstractIOSArtifactsBuilder<T extends AbstractIOSArtifactInfo> {

    @Inject IOSConfiguration conf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject CommandExecutor executor

    abstract void buildArtifacts(T info)

    @PackageScope
    void mkdirs(FlowArtifact fa) {
        fa.location.parentFile.mkdirs()
        fa.location.delete()
    }
}
