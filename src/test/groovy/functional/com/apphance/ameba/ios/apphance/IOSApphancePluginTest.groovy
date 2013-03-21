package com.apphance.ameba.ios.apphance

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test

import static org.junit.Assert.assertTrue

class IOSApphancePluginTest {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    File projectDir

    protected void runGradle(String... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(projectDir).connect();
        try {
            def buildLauncher = connection.newBuild()
            buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
            buildLauncher.forTasks(tasks).run();
        } finally {
            connection.close();
        }
    }

    @Test
    void addApphanceTest() {
        projectDir = new File('testProjects/ios/GradleXCode')
        runGradle('clean', 'unlockKeyChain', 'build-GradleXCode-BasicConfiguration')
        assertTrue(new File(projectDir, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }

    @Test
    void addApphanceTestWithApphanceAlreadyInProject() {
        projectDir = new File('testProjects/ios/GradleXCodeWithApphance')
        runGradle('clean', 'unlockKeyChain', 'build-GradleXCodeWithApphance-BasicConfiguration')
        assertTrue(new File(projectDir, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeWithApphance/BasicConfiguration/GradleXCodeWithApphance-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }
}
