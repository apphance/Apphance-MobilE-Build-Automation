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
			'buildAll',
			'clean',
		],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
	}

	@Test
	public void testConfigurationTasksAvailable() {
		verifyTasksInGroup(getProject(),[
			'cleanConfiguration',
			'copyGalleryFiles',
			'readProjectConfiguration',
		],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
	}

	@Test
	public void testReleaseTasksAvailable() {
		verifyTasksInGroup(getProject(),[],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
	}

	@Test
	public void testSetupTasksAvailable() {
		verifyTasksInGroup(getProject(),[
			'prepareSetup',
			'showConventions',
			'showSetup',
			'verifySetup',

		],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
	}
}
