package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.BaseTaskTest
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

abstract class BaseAndroidTaskTest extends BaseTaskTest {

    protected Project getProject(boolean variants = true) {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        if (variants) {
            projectBuilder.withProjectDir(new File("testProjects/android/android-basic"))
        } else {
            projectBuilder.withProjectDir(new File("testProjects/android/android-novariants"))
        }
        Project project = projectBuilder.build()
        project.project.plugins.apply(ProjectConfigurationPlugin.class)
        return project
    }

}
