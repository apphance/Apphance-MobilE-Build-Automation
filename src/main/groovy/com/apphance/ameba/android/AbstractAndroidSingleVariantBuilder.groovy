package com.apphance.ameba.android

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.buildplugin.AndroidBuildListener
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Base builder. Builds binary files - APK or JAR- depends on implementation.
 *
 */
abstract class AbstractAndroidSingleVariantBuilder {

    static Logger logger = Logging.getLogger(AbstractAndroidSingleVariantBuilder.class)
    Project project
    ProjectHelper projectHelper
    static Collection<AndroidBuildListener> buildListeners = []

    ProjectConfiguration conf
    AndroidProjectConfiguration androidConf
    File variantsDir

    AbstractAndroidSingleVariantBuilder(Project project, AndroidProjectConfiguration androidProjectConfiguration) {
        use(PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.androidConf = androidProjectConfiguration
            this.variantsDir = project.file("variants")
        }
    }

    File getTmpDirectory(Project project, String variant) {
        return new File(project.rootDir.parent, ("tmp-${project.rootDir.name}-" + variant).replaceAll('[\\\\ /]', '_'))
    }

    String getDebugRelease(Project project, String variant) {
        File dir = project.file("variants/${variant}")
        if (!dir.exists()) {
            return variant //Debug/Release
        }
        boolean marketVariant = dir.list().any { it == 'market_variant.txt' }
        return marketVariant ? 'Release' : 'Debug'
    }


    void updateAndroidConfigurationWithVariants() {
        androidConf.variants = []
        if (hasVariants()) {
            androidConf.variants = getVariants()
        } else {
            androidConf.variants = ["Debug", "Release"]
        }
        if (androidConf.variants.empty) {
            throw new GradleException("variants directory should contain at least one variant!")
        }
        androidConf.variants.each { variant ->
            androidConf.tmpDirs[variant] = getTmpDirectory(project, variant)
            androidConf.debugRelease[variant] = getDebugRelease(project, variant)
        }
    }

    protected boolean hasVariants() {
        return variantsDir.exists() && variantsDir.isDirectory()
    }

    protected Collection<String> getVariants() {
        def variants = []
        variantsDir.eachDir {
            if (!androidConf.isBuildExcluded(it.name)) {
                variants << it.name
            }
        }
        return variants
    }

    abstract void buildSingle(AndroidBuilderInfo bi)
}
