package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*;
import org.junit.*

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import com.apphance.ameba.ProjectHelper;

class IOSApphancePluginTest {

	File projectDir

	protected void runGradle(String ... tasks) {
		def cmd = ['gradle']
		cmd << '--stacktrace'
		tasks.each { cmd << it }
		ProcessBuilder processBuilder = new ProcessBuilder()
		processBuilder.command(cmd).directory(projectDir).redirectErrorStream(true)
		Process process = processBuilder.start()
		Thread outputThread = ProcessGroovyMethods.consumeProcessOutputStream(process, System.out)
		process.waitFor()
	}

	@Test
	void addApphanceTest() {
		projectDir = new File('testProjects/ios/GradleXCode')
		runGradle('clean', 'build-GradleXCode-BasicConfiguration')
		assertTrue(new File(projectDir, "ota/ssasdadasdasd/1-SNAPSHOT_1/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1-SNAPSHOT_1.ipa").exists())
	}

	@Test
	void addApphanceTestWithApphanceAlreadyInProject() {
		projectDir = new File('testProjects/ios/GradleXCodeWithApphance')
		runGradle('clean', 'build-GradleXCodeWithApphance-BasicConfiguration')
		assertTrue(new File(projectDir, "ota/ssasdadasdasd/1-SNAPSHOT_1/GradleXCodeWithApphance/BasicConfiguration/GradleXCodeWithApphance-BasicConfiguration-1-SNAPSHOT_1.ipa").exists())
	}
}
