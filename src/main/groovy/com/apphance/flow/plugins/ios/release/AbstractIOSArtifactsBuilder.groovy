package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactInfo
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.transform.PackageScope

import javax.inject.Inject

abstract class AbstractIOSArtifactsBuilder {

    @Inject IOSConfiguration conf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSArtifactProvider artifactProvider
    @Inject CommandExecutor executor

    abstract void buildArtifacts(IOSArtifactInfo bi)

    @PackageScope
    void mkdirs(FlowArtifact fa) {
        fa.location.parentFile.mkdirs()
        fa.location.delete()
    }
}
