package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.builder.IOSBuilderInfo

import javax.inject.Inject

abstract class AbstractIOSArtifactsBuilder {

    @Inject IOSConfiguration conf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject CommandExecutor executor

    abstract void buildArtifacts(IOSBuilderInfo bi)
}
