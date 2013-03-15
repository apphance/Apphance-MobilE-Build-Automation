package com.apphance.ameba.android.plugins.jarlibrary.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

//TODO be tested & refactored
@Mixin(AndroidJarLibraryMixin)
class DeployJarLibraryTask {

    private Project project
    private ProjectConfiguration conf
    private AndroidProjectConfiguration androidConf

    DeployJarLibraryTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.androidConf = getAndroidProjectConfiguration(project)
    }

    Closure deployJarLibrary = {
        repositories {
            mavenDeployer {
                pom.version = pom.version == '0' ? conf.versionString : pom.version
            }
        }
        artifacts {
            jarLibraryConfiguration file: project.file(getJarLibraryFilePath(androidConf.mainProjectName, conf.versionString)), name: androidConf.mainProjectName
        }
    }
}
