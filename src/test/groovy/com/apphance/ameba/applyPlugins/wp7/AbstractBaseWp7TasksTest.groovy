package com.apphance.ameba.applyPlugins.wp7

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder


import com.apphance.ameba.BaseTaskTest
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin

abstract class AbstractBaseWp7TasksTest extends BaseTaskTest {

	protected Project getProject() {
		ProjectBuilder projectBuilder = ProjectBuilder.builder()
		projectBuilder.withProjectDir(new File("testProjects/wp7"))
		Project project = projectBuilder.build()
		project.project.plugins.apply(ProjectConfigurationPlugin.class)
		return project
	}
}
