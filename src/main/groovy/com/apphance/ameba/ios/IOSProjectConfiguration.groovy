package com.apphance.ameba.ios

import java.io.File
import java.util.Collection
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.regex.Pattern

import com.apphance.ameba.AmebaArtifact

class IOSProjectConfiguration {
    String sdk
    String simulatorsdk
    String mainTarget
    String mainConfiguration
    String foneMonkeyConfiguration
    String KIFConfiguration
    File distributionDirectory
    List<String> targets = []
    List<String> configurations = []
    List<String> alltargets = []
    List<String> allconfigurations = []
    Map<String,AmebaArtifact> distributionZipFiles = [:]
    Map<String,AmebaArtifact> dSYMZipFiles = [:]
    Map<String,AmebaArtifact> ipaFiles = [:]
    Map<String,AmebaArtifact> manifestFiles = [:]
    Map<String,AmebaArtifact> mobileProvisionFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
    Map<String, AmebaArtifact> foneMonkeyTestResultFiles = [:]
    Map<String,AmebaArtifact> dmgImageFiles = [:]
    List<String> families = []
    File plistFile
    def monkeyTests = [:]
    def monkeyTestResults = [:]
    Map<String, HashMap<String, Collection<AmebaArtifact>>> monkeyTestImages = [:]
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
