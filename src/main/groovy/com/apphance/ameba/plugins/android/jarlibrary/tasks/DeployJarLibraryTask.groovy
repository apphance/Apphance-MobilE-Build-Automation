package com.apphance.ameba.plugins.android.jarlibrary.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.google.inject.Inject
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

//TODO be tested & refactored
@Mixin(AndroidJarLibraryMixin)
class DeployJarLibraryTask extends DefaultTask {

    static String NAME = 'deployJarLibrary'
    String group = AMEBA_BUILD
    String description = 'Deploys jar library to maven repository'

    @Inject
    private AndroidConfiguration androidConf

    @Override
    Task doFirst(Action<? super Task> action) {
        return super.doFirst(deployJarLibrary)
    }

    private Closure deployJarLibrary = {
        repositories {
            mavenDeployer {
                pom.version = pom.version == '0' ? androidConf.versionString : pom.version
            }
        }
        artifacts {
            jarLibraryConfiguration file: project.file(getJarLibraryFilePath(androidConf.projectName.value, androidConf.versionString)),
                    name: androidConf.projectName.value
        }
    }
}
