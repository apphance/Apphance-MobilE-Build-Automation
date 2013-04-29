package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.android.release.AndroidBuildListener
import org.gradle.api.logging.Logger

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

/**
 * Base builder. Builds binary files - APK or JAR- depends on implementation.
 *
 */
abstract class AbstractAndroidSingleVariantBuilder {

    Logger logger = getLogger(getClass())
    Collection<AndroidBuildListener> buildListeners = []

    @Inject
    AntBuilder ant
    @Inject
    AntExecutor antExecutor
    @Inject
    AndroidVariantsConfiguration variantsConfiguration

    abstract void buildSingle(AndroidBuilderInfo bi)

    void registerListener(AndroidBuildListener listener) {
        buildListeners << listener
    }
}
