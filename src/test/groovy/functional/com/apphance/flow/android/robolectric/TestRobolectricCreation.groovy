package com.apphance.flow.android.robolectric

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals

class TestRobolectricCreation {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    private static File conventionsBase = new File("testProjects/android/android-robolectric-create");
    private static File roboPath = new File(conventionsBase.path + '/test/robolectric')

    private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, dirName)
        return GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    @Before
    void cleanDirectories() {
        roboPath.deleteDir()
    }

    @Test
    public void testCreatingRobolectric() throws Exception {
        assert !roboPath.exists()
        ProjectConnection connection = getProjectConnection(conventionsBase, "")
        try {
            BuildLauncher bl = connection.newBuild().forTasks('prepareRobolectric');
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            bl.setStandardOutput(baos)
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()
            String output = baos.toString('utf-8')
            println output
        } finally {
            connection.close()
        }
        assert roboPath.exists()
        assert new File(roboPath.path + '/libs/').list().findAll { it.matches('robolectric.*\\.jar') }.size() == 1
        assert new File(roboPath.path + '/libs/').list().findAll { it.matches('junit.*\\.jar') }.size() == 1
    }

    @Test
    public void testReCreatingRobolectric() {
        ProjectConnection connection = getProjectConnection(conventionsBase, "")
        try {
            def f = new File(roboPath.path + '/build.gradle');
            assert !f.exists()
            BuildLauncher bl = connection.newBuild().forTasks('prepareRobolectric');
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()

            String text = f.getText()

            def replace = text.replace('f', 'd')

            f.write(replace)

            bl = connection.newBuild().forTasks('prepareRobolectric');
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()

            def newText = f.getText()
            assertEquals(newText, text)
        } finally {
            connection.close()
        }

        assert roboPath.exists()
    }
}
