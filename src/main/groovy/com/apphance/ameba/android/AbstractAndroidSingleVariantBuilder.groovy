package com.apphance.ameba.android

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.buildplugin.AndroidBuildListener
import com.apphance.ameba.executor.command.CommandExecutor
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

    CommandExecutor executor
    ProjectConfiguration conf
    AndroidProjectConfiguration androidConf
    File variantsDir

    AbstractAndroidSingleVariantBuilder(Project project, AndroidProjectConfiguration androidProjectConfiguration, CommandExecutor executor) {
        use(PropertyCategory) {
            this.project = project
            this.conf = project.getProjectConfiguration()
            this.androidConf = androidProjectConfiguration
            this.variantsDir = project.file('variants')
            this.executor = executor
        }
    }

    abstract void buildSingle(AndroidBuilderInfo bi)
}
