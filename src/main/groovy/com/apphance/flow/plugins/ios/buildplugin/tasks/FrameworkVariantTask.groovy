package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSFrameworkArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.util.FlowUtils

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.FRAMEWORK
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

class FrameworkVariantTask extends AbstractBuildVariantTask {

    String description = "Prepares 'framework' file for given variant"

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSFrameworkArtifactsBuilder frameworkArtifactsBuilder
    @Inject IOSArtifactProvider artifactProvider
    protected FlowUtils fu = new FlowUtils()

    @Lazy File simTmpDir = { fu.temporaryDir }()
    @Lazy File deviceTmpDir = { fu.temporaryDir }()

    @Override
    void build() {
        checkNotNull(variant, 'Null variant passed to builder!')
        checkArgument(variant.mode.value == FRAMEWORK, "Invalid build mode: $variant.mode.value!")

        iosExecutor.buildVariant(variant.tmpDir, cmdSim)
        iosExecutor.buildVariant(variant.tmpDir, cmdDevice)

        if (releaseConf.isEnabled()) {
            def info = artifactProvider.frameworkInfo(variant)
            info.simLib = new File(simTmpDir, 'libsim.a')
            info.deviceLib = new File(deviceTmpDir, 'libdevice.a')
            info.headers = variant.frameworkHeaders.value
            info.resources = variant.frameworkResources.value
            frameworkArtifactsBuilder.buildArtifacts(info)
        }
    }

    @Lazy
    List<String> cmdSim = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] +
                ['-sdk', conf.simulatorSdk.value ?: 'iphonesimulator'] + ['-arch', 'i386'] +
                ['-configuration', variant.archiveConfiguration] +
                ["CONFIGURATION_BUILD_DIR=${simTmpDir.absolutePath}".toString()] +
                ['PRODUCT_NAME=sim'] +
                ['clean', 'build']
    }()

    @Lazy
    List<String> cmdDevice = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] +
                ['-sdk', conf.sdk.value ?: 'iphoneos'] +
                ['-configuration', variant.archiveConfiguration] +
                ["CONFIGURATION_BUILD_DIR=${deviceTmpDir.absolutePath}".toString()] +
                ['PRODUCT_NAME=device'] +
                ['clean', 'build']
    }()
}
