package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSFrameworkArtifactsBuilder

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.FRAMEWORK

class FrameworkVariantTask extends AbstractBuildVariantTask {

    String description = "Prepares 'framework' file for given variant"

    @Inject IOSFrameworkArtifactsBuilder frameworkArtifactsBuilder

    @Lazy File simTmpDir = { fu.temporaryDir }()
    @Lazy File deviceTmpDir = { fu.temporaryDir }()

    final IOSBuildMode validationMode = FRAMEWORK

    @Override
    void build() {
        validate()

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
        variant.xcodebuildExecutionPath + ['-scheme', variant.schemeName] +
                ['-sdk', conf.simulatorSdk.value ?: 'iphonesimulator'] + ['-arch', 'i386'] +
                ['-configuration', variant.archiveConfiguration] +
                ["CONFIGURATION_BUILD_DIR=${simTmpDir.absolutePath}".toString()] +
                ['PRODUCT_NAME=sim'] +
                ['clean', 'build']
    }()

    @Lazy
    List<String> cmdDevice = {
        variant.xcodebuildExecutionPath + ['-scheme', variant.schemeName] +
                ['-sdk', conf.sdk.value ?: 'iphoneos'] +
                ['-configuration', variant.archiveConfiguration] +
                ["CONFIGURATION_BUILD_DIR=${deviceTmpDir.absolutePath}".toString()] +
                ['PRODUCT_NAME=device'] +
                ['clean', 'build']
    }()
}
