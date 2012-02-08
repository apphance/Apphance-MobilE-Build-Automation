package com.apphance.ameba.applyPlugins.ios

import java.util.Collection

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import com.apphance.ameba.applyPlugins.android.BaseTaskTest;
import com.apphance.ameba.plugins.ProjectConfigurationPlugin

abstract class BaseIOSTaskTest extends BaseTaskTest {
    protected Project getProject() {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/ios/GradleXCode"))
        Project project = projectBuilder.build()
        project['ios.plist.file'] = 'Test.plist'
        project['ios.distribution.resources.dir'] = 'release/distribution_resources'
        project.project.plugins.apply(ProjectConfigurationPlugin.class)
        return project
    }
}
