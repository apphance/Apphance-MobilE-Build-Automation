package com.apphance.ameba.android

import java.io.File
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.Set
import java.util.regex.Pattern

import org.gradle.util.GUtil

import com.apphance.ameba.AmebaArtifact


/**
 * TODO: This class should be split into independent pieces. This is "god" class of Android.
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
    Map<String,AmebaArtifact> apkFiles = [:]
    Map<String,AmebaArtifact> jarFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
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

    @Override
    public String toString() {
        return this.getProperties()
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
}
