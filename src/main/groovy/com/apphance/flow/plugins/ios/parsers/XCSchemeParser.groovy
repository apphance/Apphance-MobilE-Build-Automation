package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.util.Preconditions
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException

@Mixin(Preconditions)
class XCSchemeParser {

    String configurationName(File scheme) {
        def xml = parseSchemeFile(scheme)
        def conf = xml.LaunchAction.@buildConfiguration.text()
        conf
    }

    boolean isBuildable(File scheme) {
        def xml
        try { xml = parseSchemeFile(scheme) } catch (e) { return false }
        xml.LaunchAction.BuildableProductRunnable.size() != 0
    }

    String buildableName(File scheme) {
        def xml = parseSchemeFile(scheme)
        def buildableName = xml.LaunchAction.BuildableProductRunnable.BuildableReference.@BuildableName
        buildableName
    }

    String blueprintIdentifier(File scheme) {
        def xml = parseSchemeFile(scheme)
        def blueprintIdentifier = xml.LaunchAction.BuildableProductRunnable.BuildableReference.@BlueprintIdentifier
        blueprintIdentifier
    }

    private GPathResult parseSchemeFile(File scheme) {
        validate(scheme.exists() && scheme.isFile() && scheme.size() > 0, {
            throw new GradleException("Shemes must be shared! Invalid scheme file: $scheme.absolutePath")
        })
        new XmlSlurper().parse(scheme)
    }
}
