package com.apphance.ameba.android

import groovy.lang.Closure;

import java.io.File
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.Set
import java.util.regex.Pattern

import org.gradle.util.GUtil

import com.apphance.ameba.plugins.release.AmebaArtifact;


/**
 * Keeps Android-specific configuration of the project.
 */
class AndroidProjectConfiguration {
    File sdkDirectory
    String targetName
    String minSdkTargetName
    String mainVariant
    Map<String,File> tmpDirs = [:]
    Map<String,String> debugRelease = [:]
    Collection<String> variants
    Collection<File> sdkJars = []
    Collection<File> libraryJars = []
    Collection<File> linkedLibraryJars = []
    String mainProjectPackage
    String mainProjectName
    List<String> excludedBuilds = []

    public Set<File> getAllJars() {
        Set<File>  set = []as Set
        set.addAll(sdkJars)
        set.addAll(libraryJars)
        set.addAll(linkedLibraryJars)
        return set
    }

    public String getAllJarsAsPath() {
        GUtil.join(getAllJars(), File.pathSeparator)
    }

    boolean isBuildExcluded(String variant) {
        boolean excluded = false
        excludedBuilds.each {
            Pattern p = Pattern.compile(it)
            if (p.matcher(variant).matches()) {
                excluded = true
            }
        }
        return excluded
    }

    @Override
    public String toString() {
        return this.getProperties()
    }
}
