package com.apphance.ameba.applyPlugins.wp7;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.wp7.plugins.apphance.Wp7ApphancePlugin

class Wp7ApphancePluginTasksTest extends AbstractBaseWp7TasksTest {

	protected Project getProject() {
		Project project = super.getProject()
		project.project.plugins.apply(Wp7ApphancePlugin.class)
		return project
	}


	@Test
	public void testApphanceTasksAvailable() {
		verifyTasksInGroup(getProject(),[
			'extractApphanceDll',
			'removeApphanceDll',
			'addApphanceToCsProj',
			'removeApphanceFromCsProj',
			'addApphanceToAppCs',
			'removeApphanceFromAppCs',
			'convertLogsToApphance',
			'convertLogsToSystemDebug',
		],AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE)
	}
}
