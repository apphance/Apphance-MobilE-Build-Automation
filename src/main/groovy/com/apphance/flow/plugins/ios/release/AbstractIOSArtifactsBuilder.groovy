package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo

import javax.inject.Inject

abstract class AbstractIOSArtifactsBuilder {

    @Inject IOSConfiguration conf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject CommandExecutor executor

    abstract void buildArtifacts(IOSBuilderInfo bi)
}
