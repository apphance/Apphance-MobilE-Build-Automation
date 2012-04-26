package com.apphance.ameba.applyPlugins.android;

import java.io.File;

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.apphance.ameba.ProjectHelper

class TestRobolectricCreation {
	private static File conventionsBase = new File("testProjects/android-robolectric-create");
	private static File roboPath = new File(conventionsBase.path + '/test/robolectric')

	private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
		def projectDir = new File(baseFolder, dirName)
		return  GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
	}

	@Before
	void cleanDirectories(){
		roboPath.deleteDir()
	}

	@Test
	public void testCreatingRobolectric() throws Exception {
		assert !roboPath.exists()
		ProjectConnection connection = getProjectConnection(conventionsBase,"")
		try {
			BuildLauncher bl = connection.newBuild().forTasks('createRobolectricTestStructure');
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			bl.setStandardOutput(baos)
			bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
			bl.run()
			String output = baos.toString('utf-8')
			println output
		} finally {
			connection.close()
		}
		assert roboPath.exists()
		assert new File(roboPath.path + '/libs/').list().findAll {it.matches('robolectric.*\\.jar')}.size() == 1
		assert new File(roboPath.path + '/libs/').list().findAll {it.matches('junit.*\\.jar')}.size() == 1
	}

	@Test
	public void testReCreatingRobolectric(){
		ProjectConnection connection = getProjectConnection(conventionsBase,"")
		try {
			BuildLauncher bl = connection.newBuild().forTasks('createRobolectricTestStructure');
			bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
			bl.run()

			def f = new File(roboPath.path + '/build.gradle');
			assert f.exists()
			String text = f.getText()
			
			f.write(text.replace('f', 'd'))


			bl = connection.newBuild().forTasks('createRobolectricTestStructure');
			bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
			bl.run()
			
			
			assertEquals( f.getText(), text)
		} finally {
			connection.close()
		}

		assert roboPath.exists()
	}
}
