package com.apphance.ameba.applyPlugins.android

import java.io.File;
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.Before;
import org.junit.Test;

import com.apphance.ameba.ProjectHelper

class TestRobotiumCreation {
	private static File conventionsBase = new File("testProjects/android-robotium-create");
	private static File roboPath = new File(conventionsBase.path + '/test/android')

	private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
		def projectDir = new File(baseFolder, dirName)
		return  GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
	}

	@Before
	void cleanDirectories(){
		roboPath.deleteDir()
		ProjectConnection connection = getProjectConnection(conventionsBase,"")
		try {
			BuildLauncher bl = connection.newBuild().forTasks('prepareRobotium');
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			bl.setStandardOutput(baos)
			bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
			bl.run()
			String output = baos.toString('utf-8')
			println output
		} finally {
			connection.close()
		}
	}

	@Test
	public void testCreationAndroidTest(){
		assert roboPath.exists()
		assert new File(roboPath.path + "/AndroidManifest.xml").exists()
	}
	
	@Test
	public void testInstrumentationChange(){
		File manifest = new File(roboPath.path + "/AndroidManifest.xml")
		def manifestXML = new XmlSlurper().parseText(manifest.text)
		assert manifestXML.instrumentation.@'android:name'.toString().equals("pl.polidea.instrumentation.PolideaInstrumentationTestRunner")
		
		assert new File(roboPath.path + '/libs/').list().findAll {it.matches('the-missing-android-xml-junit-test-runner.*\\.jar')}.size() == 1
	}
	
	
}
