package com.apphance.flow.plugins.ios.buildplugin.tasks

import static com.apphance.flow.configuration.ios.IOSBuildMode.FRAMEWORK
import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

class BuildVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'build' action for single variant"

    @Override
    void build() {
        checkNotNull(variant, 'Null variant passed to builder!')
        checkArgument(variant.mode.value != FRAMEWORK, "Invalid build mode: $FRAMEWORK!")
        iosExecutor.buildVariant(variant.tmpDir, cmd)
    }

    @Lazy
    List<String> cmd = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] + sdkCmd + archCmd + ['clean', 'build']
    }()
}
