package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.plugins.android.release.AndroidBuildListener
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import static org.gradle.api.logging.Logging.getLogger

/**
 * Base builder. Builds binary files - APK or JAR- depends on implementation.
 *
 */
abstract class AbstractAndroidSingleVariantBuilder {

    Logger logger = getLogger(getClass())
    static Collection<AndroidBuildListener> buildListeners = []

    Project project
    AndroidConfiguration androidConf
    File variantsDir

    AbstractAndroidSingleVariantBuilder(Project project, AndroidConfiguration androidConf) {
        this.project = project
        this.androidConf = androidConf
        this.variantsDir = project.file('variants')//TODO
    }

    abstract void buildSingle(AndroidBuilderInfo bi)

}
