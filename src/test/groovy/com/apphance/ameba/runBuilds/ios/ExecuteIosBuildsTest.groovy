package com.apphance.ameba.runBuilds.ios

import com.apphance.ameba.ProjectHelper
import org.gradle.tooling.BuildException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

class ExecuteIosBuildsTest {

    static File testProjectMoreVariants = new File("testProjects/ios-morevariants/GradleXCodeMoreVariants")
    static File testProjectOneVariant = new File("testProjects/ios/GradleXCode")
    static File testProjectNoVersion = new File("testProjects/ios/GradleXCodeNoVersion")
    static File testProjectNoVersionString = new File("testProjects/ios/GradleXCodeNoVersionString")
    static File templateFile = new File("templates/ios")
    static ProjectConnection connection
    static ProjectConnection gradleWithPropertiesConnection
    static ProjectConnection gradleOneVariantConnection
    static ProjectConnection gradleNoVersionConnection
    static ProjectConnection gradleNoVersionStringConnection

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        gradleWithPropertiesConnection = GradleConnector.newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        gradleOneVariantConnection = GradleConnector.newConnector().forProjectDirectory(testProjectOneVariant).connect();
        gradleNoVersionConnection = GradleConnector.newConnector().forProjectDirectory(testProjectNoVersion).connect();
        gradleNoVersionStringConnection = GradleConnector.newConnector().forProjectDirectory(testProjectNoVersionString).connect();
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
        gradleWithPropertiesConnection.close()
        gradleOneVariantConnection.close()
        gradleNoVersionConnection.close()
        gradleNoVersionStringConnection.close()
    }

    protected void runGradleMoreVariants(String... tasks) {
        def buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleWithProperties(Properties p, String... tasks) {
        def buildLauncher = gradleWithPropertiesConnection.newBuild()
        def args = p.collect { property, value -> "-D${property}=${value}" }
        ProjectHelper.GRADLE_DAEMON_ARGS.each { args << it }
        buildLauncher.setJvmArguments(args as String[])
        buildLauncher.forTasks(tasks).run()
    }

    protected void runGradleOneVariant(String... tasks) {
        def buildLauncher = gradleOneVariantConnection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleNoVersion(String... tasks) {
        def buildLauncher = gradleNoVersionConnection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleNoVersionString(String... tasks) {
        def buildLauncher = gradleNoVersionStringConnection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }


    @Test
    void testCleanCheckTests() {
        runGradleMoreVariants('clean', 'checkTests')
        assertFalse(new File(testProjectMoreVariants, "bin").exists())
        assertFalse(new File(testProjectMoreVariants, "build").exists())
    }

    @Test
    void testOta() {
        runGradleMoreVariants('cleanRelease')
        assertTrue(new File(testProjectMoreVariants, "ota").exists())
        assertEquals(0, new File(testProjectMoreVariants, "ota").listFiles().length)
        assertTrue(new File(testProjectMoreVariants, "tmp").exists())
        assertEquals(0, new File(testProjectMoreVariants, "tmp").listFiles().length)
    }

    @Test
    void testBuildOneVariant() {
        runGradleOneVariant('unlockKeyChain', 'buildAll')
        assertTrue(new File(testProjectOneVariant,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
        assertTrue(new File(testProjectOneVariant,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32.mobileprovision").exists())
        assertTrue(new File(testProjectOneVariant,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32.zip").exists())
        assertTrue(new File(testProjectOneVariant,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32_dSYM.zip").exists())
        assertFalse(new File(testProjectOneVariant,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/AnotherConfiguration/GradleXCode-AnotherConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }


    @Test
    void testBuildMoreVariants() {
        runGradleMoreVariants('unlockKeyChain', 'buildAll')
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/BasicConfiguration/GradleXCodeMoreVariants-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/BasicConfiguration/GradleXCodeMoreVariants-BasicConfiguration-1.0-SNAPSHOT_32.mobileprovision").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/BasicConfiguration/GradleXCodeMoreVariants-BasicConfiguration-1.0-SNAPSHOT_32.zip").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/BasicConfiguration/GradleXCodeMoreVariants-BasicConfiguration-1.0-SNAPSHOT_32_dSYM.zip").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariants-AnotherConfiguration-1.0-SNAPSHOT_32.ipa").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariants-AnotherConfiguration-1.0-SNAPSHOT_32.mobileprovision").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariants-AnotherConfiguration-1.0-SNAPSHOT_32.zip").exists())
        assertTrue(new File(testProjectMoreVariants,
                "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariants-AnotherConfiguration-1.0-SNAPSHOT_32_dSYM.zip").exists())
    }

    @Test
    void testUpdateVersion() {
        Properties p = new Properties()
        p.setProperty("version.string", "NEWVERSION")
        File original = new File(testProjectMoreVariants, 'GradleXCodeMoreVariants/GradleXCodeMoreVariants-Info.plist')
        File tmp = new File(testProjectMoreVariants, 'GradleXCodeMoreVariants/GradleXCodeMoreVariants-Info.plist.orig')
        tmp.delete()
        tmp << original.text
        try {
            runGradleWithProperties(p, 'updateVersion')
            def newText = new File(original.getAbsolutePath()).text
            assertTrue(newText.contains('<string>33</string>'))
            assertTrue(newText.contains('<string>NEWVERSION</string>'))
        } finally {
            def text = original.text
            original.delete()
            original << tmp.text
        }
    }

    @Test
    void testBuildAndPrepareMoreVariantsMailMessage() {
        runGradleMoreVariants('cleanRelease', 'unlockKeyChain', 'buildAll')
        runGradleMoreVariants('prepareImageMontage', 'prepareAvailableArtifactsInfo', 'prepareMailMessage')
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/icon.png").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/plain_file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/qrcode-GradleXCodeMoreVariants-1.0-SNAPSHOT_32.png").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariants-AnotherConfiguration-1.0-SNAPSHOT_32.ipa").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/BasicConfiguration/GradleXCodeMoreVariants-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }

    @Test
    void testBuildAndPrepareMoreVariantsMailMessageWithSimulators() {
        runGradleMoreVariants('cleanRelease', 'unlockKeyChain', 'buildAll')
        runGradleMoreVariants('buildAllSimulators', 'prepareImageMontage', 'prepareAvailableArtifactsInfo', 'prepareMailMessage')
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/icon.png").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/plain_file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/qrcode-GradleXCodeMoreVariants-1.0-SNAPSHOT_32.png").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariants-AnotherConfiguration-1.0-SNAPSHOT_32.ipa").exists())
        assertTrue(new File(testProjectMoreVariants, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/BasicConfiguration/GradleXCodeMoreVariants-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }

    @Test
    void testBuildAndPrepareOneVariantMailMessage() {
        runGradleOneVariant('cleanRelease', 'unlockKeyChain', 'buildAll')
        runGradleOneVariant('prepareImageMontage', 'prepareMailMessage')
        assertTrue(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/file_index.html").exists())
        assertTrue(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/icon.png").exists())
        assertTrue(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/index.html").exists())
        assertTrue(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/plain_file_index.html").exists())
        assertTrue(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/qrcode-GradleXCode-1.0-SNAPSHOT_32.png").exists())
        assertFalse(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/AnotherConfiguration/GradleXCode-AnotherConfiguration-1.0-SNAPSHOT_32.ipa").exists())
        assertTrue(new File(testProjectOneVariant, "ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCode/BasicConfiguration/GradleXCode-BasicConfiguration-1.0-SNAPSHOT_32.ipa").exists())
    }

    @Test
    void testBuildNoVersion() {
        try {
            runGradleNoVersion('cleanRelease', 'buildAll')
            fail("There should be a version exception thrown!")
        } catch (BuildException e) {
            String message = e.cause.cause.cause.message
            assertTrue("Wrong message: " + message, message.contains("The CFBundleVersion key is missing"))
        }
    }

    @Test
    void testBuildNoVersionString() {
        try {
            runGradleNoVersionString('cleanRelease', 'buildAll')
            fail("There should be a version exception thrown!")
        } catch (BuildException e) {
            String message = e.cause.cause.cause.message
            assertTrue("Wrong message: " + message, message.contains("The CFBundleShortVersionString key is missing"))
        }
    }

    @Test
    void testBuildSimulatorsNoVersion() {
        try {
            runGradleNoVersion('cleanRelease', 'buildAllSimulators')
            fail("There should be a version exception thrown!")
        } catch (BuildException e) {
            String message = e.cause.cause.cause.message
            assertTrue("Wrong message: " + message, message.contains("The CFBundleVersion key is missing"))
        }
    }

    @Test
    void testBuildSimulatorsNoVersionString() {
        try {
            runGradleNoVersionString('cleanRelease', 'buildAllSimulators')
            fail("There should be a version exception thrown!")
        } catch (BuildException e) {
            String message = e.cause.cause.cause.message
            assertTrue("Wrong message: " + message, message.contains("The CFBundleShortVersionString key is missing"))
        }
    }

    @Test
    void testBuildAllSimulators() {
        runGradleMoreVariants('buildAllSimulators')
        File fileIphone = new File(testProjectMoreVariants, 'ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/Debug/GradleXCodeMoreVariants-Debug-1.0-SNAPSHOT_32-iphone-simulator-image.dmg')
        File fileIpad = new File(testProjectMoreVariants, 'ota/ssasdadasdasd/1.0-SNAPSHOT_32/GradleXCodeMoreVariants/Debug/GradleXCodeMoreVariants-Debug-1.0-SNAPSHOT_32-iPad-simulator-image.dmg')
        assertTrue(fileIphone.exists())
        assertTrue(fileIphone.size() > 30000)
        assertTrue(fileIpad.exists())
        assertTrue(fileIpad.size() > 30000)
    }

    @Test
    void testPrepareAvailableArtifacts() {
        runGradleMoreVariants('buildAllSimulators', 'prepareImageMontage', 'prepareAvailableArtifactsInfo')
    }

    @Test
    void testBuildSourcesZip() {
        runGradleMoreVariants('buildSourcesZip')
        File file = new File(testProjectMoreVariants, 'tmp/GradleXCodeMoreVariants-1.0-SNAPSHOT_32-src.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }
}
