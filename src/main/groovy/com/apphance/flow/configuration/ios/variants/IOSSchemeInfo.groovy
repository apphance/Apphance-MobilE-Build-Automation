package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import groovy.transform.PackageScope

import javax.inject.Inject

import static java.io.File.separator

class IOSSchemeInfo {

    @Inject IOSConfiguration conf
    @Inject XCSchemeParser schemeParser

    @Lazy
    @PackageScope
    boolean hasSchemes = {
        schemesDeclared() && schemesShared() && schemesBuildable() && schemesHasSingleBuildableTarget()
    }()

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
    boolean schemesBuildable() {
        schemeFiles.any(this.&schemeBuildable)
    }

    @PackageScope
    boolean schemeBuildable(File scheme) {
        schemeParser.isBuildable(scheme)
    }

    @PackageScope
    boolean schemesHasSingleBuildableTarget() {
        schemeFiles.any(this.&schemeHasSingleBuildableTarget)
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
        new File(conf.xcodeDir.value, "xcshareddata${separator}xcschemes$separator${name}.xcscheme")
    }.memoize()

}
