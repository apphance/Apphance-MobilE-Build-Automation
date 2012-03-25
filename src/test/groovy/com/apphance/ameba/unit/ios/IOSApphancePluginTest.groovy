package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.*

import com.apphance.ameba.ProjectHelper

class IOSApphancePluginTest {

    File projectDir

    protected void runGradle(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(projectDir).connect();
        try {
            def buildLauncher = connection.newBuild()
            buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
            buildLauncher.forTasks(tasks).run();
        } finally {
            connection.close();
        }
    }

    @Test
    void addApphanceTest() {
        projectDir = new File('testProjects/ios/GradleXCode')
        runGradle('clean', 'build-GradleXCode-BasicConfiguration')
        assertTrue(new File(projectDir, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }

    @Test
    void addApphanceTestWithApphanceAlreadyInProject() {
        projectDir = new File('testProjects/ios/GradleXCodeWithApphance')
        runGradle('clean', 'build-GradleXCodeWithApphance-BasicConfiguration')
        assertTrue(new File(projectDir, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeWithApphance/BasicConfiguration/GradleXCodeWithApphance-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }
}
