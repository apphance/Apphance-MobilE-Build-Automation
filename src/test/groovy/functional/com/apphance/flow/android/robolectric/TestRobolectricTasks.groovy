package com.apphance.flow.android.robolectric

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Test

class TestRobolectricTasks {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    private static File conventionsBase = new File('testProjects/android/android-robolectric-test');
    private static File roboPath = new File(conventionsBase.path + '/test/robolectric')

    private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, dirName)
        return GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    @Before
    void cleanDirectories() {
        new File(roboPath.path + '/build').deleteDir()
    }

    @Test
    public void testBuildingRobolectric() throws Exception {

        ProjectConnection connection = getProjectConnection(conventionsBase, "")
        try {
            BuildLauncher bl = connection.newBuild().forTasks('prepareRobolectric');
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()

            bl = connection.newBuild().forTasks('testRobolectric');
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            bl.setStandardOutput(baos)
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()
            String output = baos.toString('utf-8')
            assert output.contains("Running test: test myFirstRobolectricTest(com.apphance.flowTest.android.MyFirstTest)")
            assert output.contains('BUILD SUCCESSFUL')
        } finally {
            connection.close()
        }
    }

    @After
    public void deleteLibs() {
        new File(roboPath.path + '/libs').delete()
    }
}
