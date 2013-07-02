package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSSchemeVariant
import com.apphance.flow.executor.IOSExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class ArchiveVariantTask extends DefaultTask {

    String group = FLOW_BUILD
    String description = 'Archives single variant for iOS'

    @Inject
    IOSExecutor executor

    IOSSchemeVariant variant

    @TaskAction
    void archiveVariant() {
        if (variant != null)
            executor.archiveVariant(variant.tmpDir, variant.archiveCmd())
        else
            logger.lifecycle('Variant archive not built - null variant passed')
    }
}
