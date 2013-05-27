package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.builder.IOSArtifactProvider
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.gradle.api.logging.Logging.getLogger

/**
 * Builds single variant for iOS projects.
 *
 */
@Singleton
class IOSSingleVariantBuilder {

    def l = getLogger(getClass())

    private Set<IOSBuildListener> buildListeners = []

    @Inject
    IOSExecutor executor
    @Inject
    IOSArtifactProvider artifactProvider
    @Inject
    PlistParser plistParser

    void registerListener(IOSReleaseListener listener) {
        buildListeners << listener
    }

    void buildVariant(AbstractIOSVariant variant) {
        def newBundleId = variant.bundleId.value
        if (isNotBlank(newBundleId)) {
            def oldBundleId = plistParser.bundleId(variant.plist)
            plistParser.replaceBundledId(variant.plist, oldBundleId, newBundleId)
            replaceBundleInAllSourceFiles(variant.tmpDir, oldBundleId, newBundleId)
        }
        executor.buildVariant(variant.tmpDir, variant.buildCmd())
        buildListeners.each {
            it.buildDone(artifactProvider.builderInfo(variant))
        }
    }

    private void replaceBundleInAllSourceFiles(File dir, String newBundleId, String oldBundleId) {
        String valueToFind = 'bundleWithIdentifier:@"' + oldBundleId
        String valueToReplace = 'bundleWithIdentifier:@"' + newBundleId
        findAllSourceFiles(dir).each { file ->
            String t = file.text
            if (t.contains(valueToFind)) {
                file.write(t.replace(valueToFind, valueToReplace))
                l.lifecycle("Replaced the $valueToFind with $valueToReplace in $file")
            }
        }
    }

    private Collection<File> findAllSourceFiles(File dir) {
        def result = []
        dir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if ((it.name.endsWith('.m') || it.name.endsWith('.h')) && !it.path.contains('/External/')) {
                l.lifecycle("Adding source file ${it} to processing list")
                result << it
            }
        }
        result
    }
}
