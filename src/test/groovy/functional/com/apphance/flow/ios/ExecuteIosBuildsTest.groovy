package com.apphance.flow.ios

import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.gradle.tooling.GradleConnector.newConnector
import static org.junit.Assert.assertTrue

class ExecuteIosBuildsTest {

    public static final List<String> GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static File testProjectMoreVariants = new File("testProjects/ios/GradleXCodeMoreVariants")
    static File testProjectOneVariant = new File("testProjects/ios/GradleXCode")
    static ProjectConnection connection
    static ProjectConnection gradleOneVariantConnection

    @BeforeClass
    static void beforeClass() {
        connection = newConnector().forProjectDirectory(testProjectMoreVariants).connect()
        gradleOneVariantConnection = newConnector().forProjectDirectory(testProjectOneVariant).connect()
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
        gradleOneVariantConnection.close()
    }

    protected void runGradleMoreVariants(String... tasks) {
        def buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run()
    }

    protected void runGradleOneVariant(String... tasks) {
        def buildLauncher = gradleOneVariantConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run();
    }

    @Test
    void testBuildOneVariant() {
        runGradleOneVariant('cleanFlow', 'archiveGradleXCode')
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCode/'
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32.ipa").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32.mobileprovision").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32.zip").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_xcarchive.zip").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_dSYM.zip").exists())
        assertTrue(new File(testProjectOneVariant, "$path/manifest.plist").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_ahSYM").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_ahSYM").listFiles().size() > 0)
    }

    @Test
    void testBuildMoreVariants() {
        runGradleMoreVariants('cleanFlow', 'archiveAllDevice')
        def path = 'flow-ota/ssasdadasdasd/1.0_32/'

        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32.ipa").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32.mobileprovision").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32.zip").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32_xcarchive.zip").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32_dSYM.zip").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/manifest.plist").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32_ahSYM").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariants/GradleXCodeMoreVariants-1.0_32_ahSYM").listFiles().size() > 0)

        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32.ipa").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32.mobileprovision").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32.zip").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32_xcarchive.zip").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32_dSYM.zip").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/manifest.plist").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32_ahSYM").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsWithApphance/GradleXCodeMoreVariantsWithApphance-1.0_32_ahSYM").listFiles().size() > 0)
    }

    @Test
    void testBuildAndPrepareMoreVariantsMailMessageWithSimulators() {
        runGradleMoreVariants('cleanFlow', 'archiveGradleXCodeMoreVariantsTests')
        def path = 'flow-ota/ssasdadasdasd/1.0_32'
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsTests/GradleXCodeMoreVariantsTests-1.0_32-iPad-simulator-image.dmg").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsTests/GradleXCodeMoreVariantsTests-1.0_32-iPhone-simulator-image.dmg").exists())
    }

    @Test
    void testBuildAllSimulators() {
        runGradleMoreVariants('cleanFlow', 'archiveAllSimulator')
        def path = 'flow-ota/ssasdadasdasd/1.0_32/GradleXCodeMoreVariantsTests/'

        File fileIphone = new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsTests-1.0_32-iPhone-simulator-image.dmg")
        File fileIpad = new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsTests-1.0_32-iPad-simulator-image.dmg")
        assertTrue(fileIphone.exists())
        assertTrue(fileIphone.size() > 30000)
        assertTrue(fileIpad.exists())
        assertTrue(fileIpad.size() > 30000)
    }
}
