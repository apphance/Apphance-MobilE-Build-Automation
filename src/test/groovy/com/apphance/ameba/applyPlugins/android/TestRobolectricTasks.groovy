package com.apphance.ameba.applyPlugins.android;

import java.io.File;

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.Before;
import org.junit.Test;

import com.apphance.ameba.ProjectHelper

class TestRobolectricTasks {
	private static File conventionsBase = new File('testProjects/android-robolectric-test');
	private static File roboPath = new File(conventionsBase.path + '/test/robolectric')

	private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
		def projectDir = new File(baseFolder, dirName)
		return  GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
	}

	@Before
	void cleanDirectories(){
		//roboPath.deleteDir()
		def f = new File(roboPath.path + '/build')
		def bool = f.deleteDir()
		println "Did I have deleted the build dir? ${bool} " + f.toString() 
		
	}
	
	@Test
	public void testBuildingRobolectric() throws Exception {
		
//		assert !roboPath.exists()
		ProjectConnection connection = getProjectConnection(conventionsBase,"")
		try {
			BuildLauncher bl = connection.newBuild().forTasks('testRobolectric');
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			bl.setStandardOutput(baos)
			bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
			bl.run()
			String output = baos.toString('utf-8')
			assert output.contains("Running test: test myFirstRobolectricTest(com.apphance.amebaTest.android.MyFirstTest)")
			assert output.contains('BUILD SUCCESSFUL')
			println output
		} finally {
			connection.close()
		}
//		assert roboPath.exists()
//		assert new File(roboPath.path + '/libs/').list().findAll {it.matches('robolectric.*\\.jar')}.size() == 1
//		assert new File(roboPath.path + '/libs/').list().findAll {it.matches('junit.*\\.jar')}.size() == 1
	}
}