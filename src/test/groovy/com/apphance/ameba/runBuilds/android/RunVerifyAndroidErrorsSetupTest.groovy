package com.apphance.ameba.runBuilds.android

import org.gradle.tooling.BuildException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.*

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class RunVerifyAndroidErrorsSetupTest {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static File testProject = new File("testProjects/android/android-basic")
    static ProjectConnection connection

    File gradleProperties = new File(testProject, "gradle.properties")
    File gradlePropertiesOrig = new File(testProject, "gradle.properties.orig")

    @Before
    void before() {
        gradlePropertiesOrig.delete()
        gradlePropertiesOrig << gradleProperties.text
    }


    @After
    void after() {
        gradleProperties.delete()
        gradleProperties << gradlePropertiesOrig.text
    }

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
    }

    @AfterClass
    static void afterClass() {
        connection.close()
    }

    String runTests(String... tasks) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        BuildLauncher bl = connection.newBuild().forTasks(tasks);
        bl.setStandardOutput(os)
        bl.setJvmArguments(GRADLE_DAEMON_ARGS)
        bl.run();
        def res = os.toString("UTF-8")
        println res
        assertFalse(res.contains('BUILD SUCCESSFUL'))
        return res
    }

    public void runErrorScenario(pattern, String replacement, String expected) {
        gradleProperties.delete()
        String newText = gradlePropertiesOrig.text.split('\n')*.
                replaceFirst(pattern, replacement).join('\n')
        println newText
        gradleProperties << newText
        try {
            runTests('verifySetup')
        } catch (BuildException e) {
            println e.cause.cause.cause.message
            assertTrue(e.cause.cause.cause.message.contains(expected))
        }
    }

    @Test
    void testMainVariantFile() {
        runErrorScenario(/^(android\.mainVariant.*)=(.*)$/, '$1=missingvariant', 'The main variant')
    }

    @Test
    void testMinSdkTargetFile() {
        runErrorScenario(/^(android\.minSdk\.target.*)=(.*)$/, '$1=missingtarget', 'The min sdk target')
    }

    @Test
    void testEmulatorNoWindow() {
        runErrorScenario(/^(android\.test\.emulator\.noWindow.*)=(.*)$/, '$1=nottruefalse', 'noWindow')
    }

    @Test
    void testEmulatorSnapshot() {
        runErrorScenario(/^(android\.test\.emulator\.snapshotEnabled.*)=(.*)$/, '$1=nottruefalse', 'snapshotEnabled')
    }

    @Test
    void testEmulatorTestPerPackage() {
        runErrorScenario(/^(android\.test\.perPackage.*)=(.*)$/, '$1=nottruefalse', 'perPackage')
    }

    @Test
    void testEmulatorUseEmma() {
        runErrorScenario(/^(android\.useEmma.*)=(.*)$/, '$1=nottruefalse', 'useEmma')
    }

    @Test
    void testDirectory() {
        runErrorScenario(/^(android\.test\.directory.*)=(.*)$/, '$1=missingdirectory', 'directory')
    }
}
