package com.apphance.flow.android.robotium

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore
class TestRobotiumCreation {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    private static File conventionsBase = new File("testProjects/android/android-robotium-create");
    private static File roboPath = new File(conventionsBase.path + '/test/android')

    private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, dirName)
        return GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    @Before
    void cleanDirectories() {
        roboPath.getParentFile().deleteDir()
        ProjectConnection connection = getProjectConnection(conventionsBase, "")
        try {
            BuildLauncher bl = connection.newBuild().forTasks('prepareRobotium');
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            bl.setStandardOutput(baos)
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()
            String output = baos.toString('utf-8')
            println output
        } finally {
            connection.close()
        }
    }

    @After
    void cleanDirs() {
        roboPath.getParentFile().deleteDir()
    }

    @Test
    public void testCreationAndroidTest() {
        assert roboPath.exists()
        assert new File(roboPath.path + "/AndroidManifest.xml").exists()
    }

    @Test
    public void testInstrumentationChange() {
        File manifest = new File(roboPath.path + "/AndroidManifest.xml")
        def manifestXML = new XmlSlurper().parseText(manifest.text)
        assert manifestXML.instrumentation.@'android:name'.toString().equals("pl.polidea.instrumentation.PolideaInstrumentationTestRunner")

        assert new File(roboPath.path + '/libs/').list().findAll { it.matches('the-missing-android-xml-junit-test-runner.*\\.jar') }.size() == 1
    }

    @Test
    public void testRobotiumLibPresence() {
        assert new File(roboPath.path + '/libs/').list().findAll { it.matches('robotium-solo.*\\.jar') }.size() == 1
    }

    @Test
    public void testNewTestClasses() {
        assert new File(roboPath.path + '/src/com/apphance/flowTest/android/BaseTestCase.java').exists()
        assert new File(roboPath.path + '/src/com/apphance/flowTest/android/TestHello.java').exists()

        assert !(new File(roboPath.path + '/src/com/apphance/flowTest/android/TestActivityTest.java').exists())
    }
}
