package com.apphance.flow.plugins.ios.buildplugin.tasks

class BuildVariantTask extends AbstractBuildVariantTask {

    String description = "Executes 'build' action for single variant"

    void build() {
        super.build()
        executor.buildVariant(variant.tmpDir, variant.buildCmd)
    }
}
