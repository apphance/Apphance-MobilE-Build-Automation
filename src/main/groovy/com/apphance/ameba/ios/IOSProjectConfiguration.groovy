package com.apphance.ameba.ios

import java.io.File
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.regex.Pattern

import com.apphance.ameba.plugins.release.AmebaArtifact;

/**
 * Keeps IOS-specific configuration for the project.
 */
class IOSProjectConfiguration {
    String sdk
    String simulatorsdk
    String mainTarget
    String mainConfiguration
    File distributionDirectory
    List<String> targets = []
    List<String> configurations = []
    List<String> alltargets = []
    List<String> allconfigurations = []
    List<String> allIphoneSDKs = []
    List<String> allIphoneSimulatorSDKs = []
    List<String> families = []
    File plistFile
    def monkeyTests = [:]
    def monkeyTestResults = [:]
    List<String> excludedBuilds = []

    @Override
    public String toString() {
        return this.getProperties()
    }

    boolean isBuildExcluded(String id) {
        boolean excluded = false
        excludedBuilds.each {
            Pattern p = Pattern.compile(it)
            if (p.matcher(id).matches()) {
                excluded = true
            }
        }
        return excluded
    }
}
