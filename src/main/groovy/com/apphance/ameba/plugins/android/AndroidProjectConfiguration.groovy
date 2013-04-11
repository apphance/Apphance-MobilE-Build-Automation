package com.apphance.ameba.plugins.android

import com.google.inject.Inject
import org.gradle.api.Project

import java.util.regex.Pattern

import static java.io.File.pathSeparator

/**
 * Keeps Android-specific configuration of the project.
 */
class AndroidProjectConfiguration {

    File rootDir
    File sdkDirectory
    String targetName
    String minSdkTargetName
    String mainProjectPackage
    String mainProjectName
    String mainVariant
    File variantsDir

    Collection<File> sdkJars = []
    Collection<File> libraryJars = []
    List<String> availableTargets //all targets available in Android SDK

    Collection<File> linkedLibraryJars = []

    //all above this comments is rewritten


    Map<String, File> tmpDirs = [:]

    Map<String, String> debugRelease = [:]

    @Override
    public String toString() {
        this.properties
    }
}
