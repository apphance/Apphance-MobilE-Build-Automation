package com.apphance.ameba.plugins.android

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.buildplugin.AndroidBuildListener
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
    static Collection<AndroidBuildListener> buildListeners = []

    ProjectConfiguration conf
    AndroidProjectConfiguration androidConf
    File variantsDir

    AbstractAndroidSingleVariantBuilder(Project project, AndroidProjectConfiguration androidProjectConfiguration) {
        use(PropertyCategory) {
            this.project = project
            this.conf = project.getProjectConfiguration()
            this.androidConf = androidProjectConfiguration
            this.variantsDir = project.file('variants')
        }
    }

    abstract void buildSingle(AndroidBuilderInfo bi)
}
