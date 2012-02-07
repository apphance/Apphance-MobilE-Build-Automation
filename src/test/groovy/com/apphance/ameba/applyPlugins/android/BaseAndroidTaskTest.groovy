package com.apphance.ameba.applyPlugins.android

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import com.apphance.ameba.plugins.ProjectConfigurationPlugin

abstract class BaseAndroidTaskTest extends BaseTaskTest {

    protected Project getProject(boolean variants=true) {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        if(variants) {
            projectBuilder.withProjectDir(new File("testProjects/android"))
        } else {
            projectBuilder.withProjectDir(new File("testProjects/android-novariants"))
        }
        Project project = projectBuilder.build()
        project.project.plugins.apply(ProjectConfigurationPlugin.class)
        return project
    }
}
