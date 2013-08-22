package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.google.common.base.Preconditions

class BuildVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'build' action for single variant"

    @Override
    void build() {
        Preconditions.checkNotNull(variant, 'Null variant passed to builder!')
        executor.buildVariant(variant.tmpDir, cmd)
    }

    @Lazy
    List<String> cmd = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] + sdkCmd + archCmd + ['clean', 'build']
    }()
}
