package com.apphance.ameba.apphance.android;

import static org.junit.Assert.*;

import java.io.File;

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper

class ApphanceOTFTest {

	boolean USE_PROCESS_EXECUTION = true
	File testNovariantsProject = new File("testProjects/android-novariants")

	protected void runGradleNoVariants(String ... tasks) {
		if (USE_PROCESS_EXECUTION) {
			def cmd = ['gradle']
			cmd << '--stacktrace'
			tasks.each { cmd << it }
			ProcessBuilder processBuilder = new ProcessBuilder()
			processBuilder.command(cmd).directory(testNovariantsProject).redirectErrorStream(true)
			Process process = processBuilder.start()
			Thread outputThread = ProcessGroovyMethods.consumeProcessOutputStream(process, System.out)
			process.waitFor()
		} else {
			ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testNovariantsProject).connect();
			try {
				connection.newBuild().forTasks(tasks).run();
			} finally {
				connection.close();
			}
		}
	}

	@Test
	void testAddAmeba() {
		File mainActivityFile = new File(testNovariantsProject, "src/com/apphance/amebaTest/android/TestActivity.java")
		File tmpCopy = new File("tmpCopy")
		tmpCopy.delete()
		tmpCopy << mainActivityFile.getText()

		runGradleNoVariants('updateProject', 'cleanRelease', 'buildDebug')
		assertTrue(new File(testNovariantsProject,
				"ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-1.0.1-SNAPSHOT_42.apk").exists())

		mainActivityFile.delete()
		mainActivityFile << tmpCopy.getText()
	}
}
