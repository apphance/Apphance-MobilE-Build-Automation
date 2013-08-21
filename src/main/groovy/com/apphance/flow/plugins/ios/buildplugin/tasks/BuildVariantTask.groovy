package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.google.common.base.Preconditions

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class BuildVariantTask extends AbstractActionVariantTask {

    String group = FLOW_BUILD
    String description = "Executes 'build' action for single variant"

    void build() {
        Preconditions.checkNotNull(variant, 'Null variant passed to builder!')
        executor.buildVariant(variant.tmpDir, cmd)
    }

    @Lazy
    List<String> cmd = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] + sdkCmd + archCmd + ['clean', 'build']
    }()
}
