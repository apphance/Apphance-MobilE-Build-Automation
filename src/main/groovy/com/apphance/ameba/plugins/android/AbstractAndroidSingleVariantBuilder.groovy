package com.apphance.ameba.plugins.android

import com.apphance.ameba.executor.AntExecutor
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
    File variantsDir
    AntExecutor antExecutor

    AbstractAndroidSingleVariantBuilder(Project project, AntExecutor antExecutor) {
        this.project = project
        this.antExecutor = antExecutor
        this.variantsDir = project.file('variants')//TODO
    }

    abstract void buildSingle(AndroidBuilderInfo bi)

}
