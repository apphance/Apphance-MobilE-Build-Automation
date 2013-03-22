package com.apphance.ameba.conventions

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test

import static org.junit.Assert.assertTrue

class TestConventions {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    private static File conventionsBase = new File("testProjects/conventions")

    private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, dirName)
        return GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    @Test
    public void testAndroidAnalysisConvention() throws Exception {
        ProjectConnection connection = getProjectConnection(conventionsBase, "ameba-android-analysis")
        try {
            BuildLauncher bl = connection.newBuild().forTasks('showConventionAndroidAnalysis');
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            bl.setStandardOutput(baos)
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()
            String output = baos.toString('utf-8')
            println output
            assertTrue(output.contains('androidAnalysis {'))
        } finally {
            connection.close()
        }
    }

    @Test
    public void testAndroidTestConvention() {
        ProjectConnection connection = getProjectConnection(conventionsBase, "ameba-android-test")
        try {
            BuildLauncher bl = connection.newBuild().forTasks('showConventionAndroidTest');
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            bl.setStandardOutput(baos)
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()
            String output = baos.toString('utf-8')
            println output
            assertTrue(output.contains('androidTest {'))
            assertTrue(output.contains('startPort'))
            assertTrue(output.contains('endPort'))
            assertTrue(output.contains('maxEmulatorStartupTime'))
            assertTrue(output.contains('retryTime'))
            assertTrue(output.contains('robotiumPath'))
            assertTrue(output.contains('robolectricPath'))
        } finally {
            connection.close()
        }
    }


}
