package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSBuildMode
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSSimulatorArtifactsBuilder

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR

class SimulatorVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'build' action for single simulator variant"

    @Inject IOSSimulatorArtifactsBuilder simulatorArtifactsBuilder
    @Inject XCSchemeParser schemeParser

    @Lazy File simTmpDir = { fu.temporaryDir }()

    final IOSBuildMode validationMode = SIMULATOR

    @Override
    void build() {
        validate()

        iosExecutor.buildVariant(variant.tmpDir, cmd)

        if (releaseConf.isEnabled()) {
            def info = artifactProvider.simInfo(variant)
            info.productName = productName
            info.appName = appName
            info.simDir = simTmpDir

            simulatorArtifactsBuilder.buildArtifacts(info)
        }
    }

    @Lazy
    List<String> cmd = {
        (conf.xcodebuildExecutionPath() + ['-scheme', variant.name] + sdkCmd + archCmd + ["CONFIGURATION_BUILD_DIR=$simTmpDir.absolutePath"] + ['clean', 'build'])
    }()
}
