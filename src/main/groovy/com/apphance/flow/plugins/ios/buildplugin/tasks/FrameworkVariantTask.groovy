package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSFrameworkArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.util.FlowUtils

import javax.inject.Inject

class FrameworkVariantTask extends AbstractBuildVariantTask {

    String description = "Prepares 'framework' file for given variant"

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSFrameworkArtifactsBuilder frameworkArtifactsBuilder
    @Inject IOSArtifactProvider artifactProvider

    protected FlowUtils fu = new FlowUtils()
    @Lazy File simTmpDir = { fu.tempDir }()
    @Lazy File deviceTmpDir = { fu.tempDir }()

    @Override
    void build() {
        iosExecutor.buildVariant(variant.tmpDir, cmdSim)
        iosExecutor.buildVariant(variant.tmpDir, cmdDevice)

        if (releaseConf.isEnabled()) {
            def info = artifactProvider.frameworkInfo(variant)
            info.simTempDir = simTmpDir
            info.deviceTempDir = deviceTmpDir
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
