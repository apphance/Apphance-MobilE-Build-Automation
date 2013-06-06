package com.apphance.ameba.plugins.android.jarlibrary.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task

import javax.inject.Inject

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_BUILD

@Mixin(AndroidJarLibraryMixin)
class DeployJarLibraryTask extends DefaultTask {

    static String NAME = 'deployJarLibrary'
    String group = FLOW_BUILD
    String description = 'Deploys jar library to maven repository'

    @Inject AndroidConfiguration androidConf

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
