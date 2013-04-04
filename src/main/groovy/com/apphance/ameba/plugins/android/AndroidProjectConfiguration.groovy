package com.apphance.ameba.plugins.android

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

    public Set<File> getAllJars() {
        Set<File> set = [] as Set
        set.addAll(sdkJars)
        set.addAll(libraryJars)
        set.addAll(linkedLibraryJars)
        return set
    }

    public String getAllJarsAsPath() {
        getAllJars().join(pathSeparator)
    }

    Collection<File> linkedLibraryJars = []

    //all above this comments is rewritten



    Map<String, File> tmpDirs = [:]

    Map<String, String> debugRelease = [:]

    List<String> excludedBuilds = []

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

    void setVariantsDir(File variantsDir) {
        this.variantsDir = variantsDir
        getVariants() //TODO disgusting :/ but temporary
    }

    Collection<String> getBuildableVariants() {
        return variants.findAll({ variant -> !isBuildExcluded(variant) })
    }

    boolean hasVariants() {
        variantsDir && variantsDir.exists() && variantsDir.isDirectory()
    }

    //TODO refactor, it does the same every time getVariants is called
    //will be fixed after auto-detection
    Collection<String> getVariants() {
        def result = []
        if (hasVariants()) {
            variantsDir.eachDir {
                if (!isBuildExcluded(it.name)) {
                    result << it.name
                }
            }
        } else {
            result.addAll(['Debug', 'Release'])
        }

        result.each { variant ->
            tmpDirs[variant] = getTmpDirectory(variant)
            debugRelease[variant] = getDebugRelease(variant)
        }

        result
    }

    private File getTmpDirectory(String variant) {
        new File(rootDir.parent, ("tmp-${rootDir.name}-" + variant).replaceAll('[\\\\ /]', '_'))
    }

    private String getDebugRelease(String variant) {
        File dir = new File(variantsDir, variant)
        if (!dir.exists()) {
            return variant //Debug/Release
        }
        boolean marketVariant = dir.list().any { it == 'market_variant.txt' }
        marketVariant ? 'Release' : 'Debug'
    }

    @Override
    public String toString() {
        this.properties
    }
}
