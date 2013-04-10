package com.apphance.ameba.plugins.android.jarlibrary.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.google.inject.Inject
import org.gradle.api.Project

//TODO be tested & refactored
@Mixin(AndroidJarLibraryMixin)
class DeployJarLibraryTask {

    @Inject Project project
    @Inject AndroidConfiguration androidConf

    Closure deployJarLibrary = {
        repositories {
            mavenDeployer {
                pom.version = pom.version == '0' ? conf.versionString : pom.version
            }
        }
        artifacts {
            jarLibraryConfiguration file: project.file(getJarLibraryFilePath(androidConf.projectName.value, androidConf.versionString.value)),
                    name: androidConf.projectName.value
        }
    }
}
