package com.apphance.ameba.runBuilds.wp7

import static org.junit.Assert.*;

import java.io.File

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test;

class RunWp7PluginTest {

	File testProject = new File("testProjects/wp7/MszePL")


	@Test
	public void testWp7PluginTasks() throws Exception {
		String res = runTests('tasks')
		println res
	}

	String runTests(String ... tasks) {
		ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream()
			BuildLauncher bl = connection.newBuild().forTasks(tasks);
			bl.setStandardOutput(os)
			bl.run();
			def res = os.toString("UTF-8")
			println res
			assertTrue(res.contains('BUILD SUCCESSFUL'))
			return res
		} finally {
			connection.close();
		}
	}
}
