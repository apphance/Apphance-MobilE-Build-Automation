package com.apphance.ameba.runBuilds.ios

import org.gradle.tooling.BuildException
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static com.apphance.ameba.ProjectHelper.GRADLE_DAEMON_ARGS
import static org.gradle.tooling.GradleConnector.newConnector
import static org.junit.Assert.*

class ExecuteIosBuildsTest {

    static File testProjectMoreVariants = new File("testProjects/ios-morevariants/GradleXCodeMoreVariants")
    static File testProjectOneVariant = new File("testProjects/ios/GradleXCode")
    static File testProjectNoVersion = new File("testProjects/ios/GradleXCodeNoVersion")
    static File testProjectNoVersionString = new File("testProjects/ios/GradleXCodeNoVersionString")
    static ProjectConnection connection
    static ProjectConnection gradleWithPropertiesConnection
    static ProjectConnection gradleOneVariantConnection
    static ProjectConnection gradleNoVersionConnection
    static ProjectConnection gradleNoVersionStringConnection

    @BeforeClass
    static void beforeClass() {
        connection = newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        gradleWithPropertiesConnection = newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        gradleOneVariantConnection = newConnector().forProjectDirectory(testProjectOneVariant).connect();
        gradleNoVersionConnection = newConnector().forProjectDirectory(testProjectNoVersion).connect();
        gradleNoVersionStringConnection = newConnector().forProjectDirectory(testProjectNoVersionString).connect();
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
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleWithProperties(Properties p, ProjectConnection pc = gradleWithPropertiesConnection, String... tasks) {
        def buildLauncher = pc.newBuild()
        def args = p.collect { property, value -> "-Dorg.gradle.project.${property}=${value}" }
        GRADLE_DAEMON_ARGS.each { args << it }
        buildLauncher.setJvmArguments(args as String[])
        buildLauncher.forTasks(tasks).run()
    }

    protected void runGradleOneVariant(String... tasks) {
        def buildLauncher = gradleOneVariantConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleNoVersion(String... tasks) {
        def buildLauncher = gradleNoVersionConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleNoVersionString(String... tasks) {
        def buildLauncher = gradleNoVersionStringConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
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
        p.setProperty('version.string', 'NEWVERSION')
        p.setProperty('version.code', '1234')
        File original = new File(testProjectMoreVariants, 'GradleXCodeMoreVariants/GradleXCodeMoreVariants-Info.plist')
        File tmp = new File(testProjectMoreVariants, 'GradleXCodeMoreVariants/GradleXCodeMoreVariants-Info.plist.orig')
        tmp.delete()
        tmp << original.text
        try {
            runGradleWithProperties(p, 'updateVersion')
            def newText = new File(original.getAbsolutePath()).text
            assertTrue(newText.contains('<string>1234</string>'))
            assertTrue(newText.contains('<string>NEWVERSION</string>'))
        } finally {
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

    @Test
    void testDefaultApphanceDependency() {
        Properties p = new Properties()
        runGradleWithProperties(p, gradleOneVariantConnection, 'clean', 'unlockKeyChain', 'build-GradleXCode-BasicConfiguration')
        def apphanceLib = new File("testProjects/ios/tmp-GradleXCode-GradleXCode-BasicConfiguration/Apphance-Pre-Production.framework")
        assertTrue(apphanceLib.exists())
        assertTrue(apphanceLib.list().length > 0)
    }

    @Test
    void testIncorrectApphanceDependency() {
        Properties p = new Properties()
        p.put('apphance.lib', 'com.apphanc:ios.production.armv7:1.8')
        try {
            runGradleWithProperties(p, gradleOneVariantConnection, 'clean', 'unlockKeyChain', 'build-GradleXCode-BasicConfiguration')
        } catch (e) {
            def c = e.cause.cause.cause
            assertEquals("Error while resolving dependency: 'com.apphanc:ios.production.armv7:1.8'", c.message)
        }
        def apphanceLib = new File("testProjects/ios/tmp-GradleXCode-GradleXCode-BasicConfiguration/Apphance-Production.framework")
        assertFalse(apphanceLib.exists())
    }

    @Test
    void testCorrectApphanceDependency() {
        Properties p = new Properties()
        p.put('apphance.lib', 'com.apphance:ios.pre-production.armv7:1.8.2')
        runGradleWithProperties(p, gradleOneVariantConnection, 'clean', 'unlockKeyChain', 'build-GradleXCode-BasicConfiguration')
        def apphanceLib = new File("testProjects/ios/tmp-GradleXCode-GradleXCode-BasicConfiguration/Apphance-Pre-Production.framework")
        assertTrue(apphanceLib.exists())
    }
}
