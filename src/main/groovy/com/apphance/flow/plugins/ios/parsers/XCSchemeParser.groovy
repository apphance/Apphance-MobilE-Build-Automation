package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.util.Preconditions
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException

import javax.inject.Inject

@Mixin(Preconditions)
class XCSchemeParser {

    @Inject IOSConfiguration conf

    String configurationName(String schemeName) {
        def xml = parseSchemeFile(schemeName)
        def conf = xml.LaunchAction.@buildConfiguration.text()
        conf
    }

    String buildableName(String schemeName) {
        def xml = parseSchemeFile(schemeName)
        def buildableName = xml.LaunchAction.BuildableProductRunnable.BuildableReference.@BuildableName
        buildableName
    }

    String blueprintIdentifier(String schemeName) {
        def xml = parseSchemeFile(schemeName)
        def blueprintIdentifier = xml.LaunchAction.BuildableProductRunnable.BuildableReference.@BlueprintIdentifier
        blueprintIdentifier
    }

    private GPathResult parseSchemeFile(String schemeName) {
        def schemeFile = schemeFile(schemeName)
        new XmlSlurper().parse(schemeFile)
    }

    private File schemeFile(String schemeName) {
        def file = new File(conf.schemesDir, "${schemeName}.xcscheme")
        validate(file.exists() && file.isFile(), {
            throw new GradleException("Shemes must be shared! Invalid scheme file: ${file.absolutePath}")
        })
        file
    }
}
