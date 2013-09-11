package com.apphance.flow.plugins.ios.scheme

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.EXCLUDE_FILTER
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

class IOSSchemeInfo {

    protected logger = getLogger(getClass())

    @Inject IOSConfiguration conf
    @Inject XCSchemeParser schemeParser

    @Lazy
    @PackageScope
    boolean hasSchemes = { schemesDeclared() && schemesShared() }()

    @PackageScope
    boolean schemesDeclared() {
        conf.schemes.size() > 0
    }

    @PackageScope
    boolean schemesShared() {
        schemeFiles.any(this.&schemeShared)
    }

    @PackageScope
    boolean schemeShared(File scheme) {
        scheme.exists() && scheme.isFile() && scheme.size() > 0
    }

    @PackageScope
    boolean schemeBuildable(File scheme) {
        schemeParser.isBuildable(scheme)
    }

    @PackageScope
    boolean schemeHasSingleBuildableTarget(File scheme) {
        schemeParser.hasSingleBuildableTarget(scheme)
    }

    @PackageScope
    boolean schemesHasEnabledTestTargets() {
        schemeFiles.any(this.&schemeHasEnabledTestTargets)
    }

    @PackageScope
    boolean schemeHasEnabledTestTargets(File scheme) {
        schemeParser.hasEnabledTestTargets(scheme)
    }

    @Lazy
    @PackageScope
    List<File> schemeFiles = {
        conf.schemes.collect { name -> schemeFile.call(name) }
    }()

    @PackageScope
    Closure<File> schemeFile = { String name ->
        logger.info("Searching scheme file for: $name in: $conf.rootDir.absolutePath")
        List<File> found = []
        conf.rootDir.traverse(
                type: FILES,
                maxDepth: MAX_RECURSION_LEVEL,
                nameFilter: ~/${name}\.xcscheme/,
                excludeFilter: EXCLUDE_FILTER
        ) {
            if (it.absolutePath.endsWith("xcshareddata/xcschemes/${name}.xcscheme"))
                found << it
        }
        logger.debug("Found following schemes for name: $name, schemes: $found")
        switch (found.size()) {
            case 0:
                logger.warn("No scheme file found for name: $name")
                return new File(conf.rootDir, "${name}.xcscheme")
            case 1:
                logger.info("Found scheme file for name: $name, file: ${found[0].absolutePath}")
                return found[0]
            default:
                throw new GradleException("Found more than one scheme file for name $name, files: $found")
        }
    }.memoize()
}
