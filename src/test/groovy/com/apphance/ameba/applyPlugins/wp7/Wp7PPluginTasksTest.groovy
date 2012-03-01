package com.apphance.ameba.applyPlugins.wp7;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.wp7.plugins.buildplugin.Wp7Plugin

class Wp7PPluginTasksTest extends AbstractBaseWp7TasksTest {

	protected Project getProject() {
		Project project = super.getProject()
		project.project.plugins.apply(Wp7Plugin.class)
		return project
	}

	@Test
	public void testBuildTasksAvailable() {
		verifyTasksInGroup(getProject(),[
			'clean',
			'buildAll',
			'checkTests',
		],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
	}

	@Test
	public void testConfigurationTasksAvailable() {
		verifyTasksInGroup(getProject(),[
			'cleanConfiguration',
			'copyGalleryFiles',
			'readProjectConfiguration',
			'showProjectConfiguration'
		],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
	}

	@Test
	public void testReleaseTasksAvailable() {
		verifyTasksInGroup(getProject(),[],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
	}

	@Test
	public void testSetupTasksAvailable() {
		verifyTasksInGroup(getProject(),[
			'prepareBaseSetup',
			'prepareSetup',
			'prepareWp7Setup',
			'showBaseSetup',
			'showSetup',
			'showWp7Setup',
			'verifyBaseSetup',
			'verifySetup',
			'verifyWp7Setup',
		],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
	}
}
