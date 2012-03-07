package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*;
import org.junit.*

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import com.apphance.ameba.ProjectHelper;

class IOSApphancePluginTest {

	File projectDir = new File('testProjects/ios/GradleXCode')

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
		runGradle('clean', 'build-GradleXCode-BasicConfiguration')
	}

}
