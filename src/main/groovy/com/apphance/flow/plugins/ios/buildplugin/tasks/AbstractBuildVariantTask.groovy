package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PlistParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static com.google.common.base.Preconditions.checkNotNull
import static groovy.io.FileType.FILES
import static org.apache.commons.lang.StringUtils.isNotBlank

abstract class AbstractBuildVariantTask extends DefaultTask {

    String group = FLOW_BUILD

    @Inject IOSExecutor executor
    @Inject PlistParser plistParser

    IOSVariant variant

    @TaskAction
    void build() {
        checkNotNull(variant, "Null variant passed to builder!")
        def newBundleId = variant.bundleId.value
        if (isNotBlank(newBundleId)) {
            def oldBundleId = plistParser.bundleId(variant.plist)
            plistParser.replaceBundledId(variant.plist, oldBundleId, newBundleId)
            replaceBundleInAllSourceFiles(variant.tmpDir, oldBundleId, newBundleId)
        }
    }

    private void replaceBundleInAllSourceFiles(File dir, String newBundleId, String oldBundleId) {
        String valueToFind = 'bundleWithIdentifier:@"' + oldBundleId
        String valueToReplace = 'bundleWithIdentifier:@"' + newBundleId
        findAllSourceFiles(dir).each { file ->
            String t = file.text
            if (t.contains(valueToFind)) {
                file.write(t.replace(valueToFind, valueToReplace))
                logger.info("Replaced the $valueToFind with $valueToReplace in $file")
            }
        }
    }

    private Collection<File> findAllSourceFiles(File dir) {
        def result = []
        dir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if ((it.name.endsWith('.m') || it.name.endsWith('.h')) && !it.path.contains('/External/')) {
                logger.info("Adding source file ${it} to processing list")
                result << it
            }
        }
        result
    }
}
