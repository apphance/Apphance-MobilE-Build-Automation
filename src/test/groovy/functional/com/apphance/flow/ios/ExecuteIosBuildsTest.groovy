package com.apphance.flow.ios

import com.apphance.flow.util.FlowUtils
import org.gradle.tooling.BuildException
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static org.gradle.tooling.GradleConnector.newConnector
import static org.junit.Assert.*

class ExecuteIosBuildsTest {

    public static final List<String> GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static File testProjectMoreVariants = new File("testProjects/ios/GradleXCodeMoreVariants")
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
        connection = newConnector().forProjectDirectory(testProjectMoreVariants).connect()
        gradleWithPropertiesConnection = newConnector().forProjectDirectory(testProjectMoreVariants).connect()
        gradleOneVariantConnection = newConnector().forProjectDirectory(testProjectOneVariant).connect()
        gradleNoVersionConnection = newConnector().forProjectDirectory(testProjectNoVersion).connect()
        gradleNoVersionStringConnection = newConnector().forProjectDirectory(testProjectNoVersionString).connect()
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
        gradleWithPropertiesConnection.close()
        gradleOneVariantConnection.close()
        gradleNoVersionConnection.close()
        gradleNoVersionStringConnection.close()
    }

    protected void runGradleMoreVariants(Properties p = null, String... tasks) {
        def buildLauncher = connection.newBuild()
        if (p) {
            def args = p.collect { property, value -> "-D${property}=${value}" }
            args.each { GRADLE_DAEMON_ARGS << it.toString() }
        }
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run()
    }

    protected void runGradleWithProperties(Properties p, ProjectConnection pc = gradleWithPropertiesConnection, String... tasks) {
        def buildLauncher = pc.newBuild()
        def args = p.collect { property, value -> "-D${property}=${value}" }
        GRADLE_DAEMON_ARGS.each { args << it }
        buildLauncher.setJvmArguments(args as String[])

        buildLauncher.forTasks(tasks).run()
    }

    protected void runGradleOneVariant(Properties p = null, String... tasks) {
        def buildLauncher = gradleOneVariantConnection.newBuild()
        if (p) {
            def args = p.collect { property, value -> "-D${property}=${value}" }
            args.each { GRADLE_DAEMON_ARGS << it.toString() }
        }
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleNoVersion(String... tasks) {
        def buildLauncher = gradleNoVersionConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleNoVersionString(String... tasks) {
        def buildLauncher = gradleNoVersionStringConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run();
    }

    @Test
    void testOta() {
        runGradleMoreVariants('cleanFlow')
        assertTrue(new File(testProjectMoreVariants, "flow-ota").exists())
        assertEquals(0, new File(testProjectMoreVariants, "flow-ota").listFiles().length)
        assertTrue(new File(testProjectMoreVariants, "flow-tmp").exists())
        assertEquals(0, new File(testProjectMoreVariants, "flow-tmp").listFiles().length)
    }

    @Test
    void testBuildOneVariant() {
        runGradleOneVariant('buildAllDevice')
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCode/BasicConfiguration'
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32.ipa").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32.mobileprovision").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32.zip").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_dSYM.zip").exists())
        assertTrue(new File(testProjectOneVariant, "$path/manifest.plist").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_ahSYM").exists())
        assertTrue(new File(testProjectOneVariant, "$path/GradleXCode-1.0_32_ahSYM").listFiles().size() > 0)
    }

    @Test
    void testBuildMoreVariants() {
        runGradleMoreVariants('buildAllDevice')
        def path = 'flow-ota/ssasdadasdasd/1.0_32/GradleXCodeMoreVariants'

        ['AnotherConfiguration', 'BasicConfiguration', 'Debug', 'Release'].each {
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/GradleXCodeMoreVariants$it-1.0_32.ipa").exists())
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/GradleXCodeMoreVariants$it-1.0_32.mobileprovision").exists())
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/GradleXCodeMoreVariants$it-1.0_32.zip").exists())
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/GradleXCodeMoreVariants$it-1.0_32_dSYM.zip").exists())
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/manifest.plist").exists())
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/GradleXCodeMoreVariants$it-1.0_32_ahSYM").exists())
            assertTrue(new File(testProjectMoreVariants,
                    "$path/$it/GradleXCodeMoreVariants$it-1.0_32_ahSYM").listFiles().size() > 0)
        }
    }

    @Test
    void testUpdateVersion() {
        Properties p = new Properties()
        p.setProperty('version.string', 'NEWVERSION')
        p.setProperty('version.code', '1234')
        runGradleWithProperties(p, 'cleanFlow', 'updateVersion')
        def variantsDir = new File(testProjectMoreVariants, TMP_DIR)
        def plists = new FlowUtils().allFiles(dir: variantsDir, where: { it.name == 'GradleXCodeMoreVariants-Info.plist' })
        assertTrue(plists.any {
            def text = it.text
            text.contains('<string>NEWVERSION</string>') && text.contains('<string>1234</string>')
        })
    }

    @Test
    void testBuildAndPrepareMoreVariantsMailMessage() {
        def p = new Properties()
        p.put('release.notes', 'some\nnotes')
        runGradleMoreVariants(p, 'cleanFlow', 'buildGradleXCodeMoreVariantsAnotherConfiguration', 'prepareImageMontage', 'prepareAvailableArtifactsInfo', 'prepareMailMessage')
        assertTrue(new File(testProjectMoreVariants, "flow-ota/ssasdadasdasd/1.0_32/file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "flow-ota/ssasdadasdasd/1.0_32/icon.png").exists())
        assertTrue(new File(testProjectMoreVariants, "flow-ota/ssasdadasdasd/1.0_32/index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "flow-ota/ssasdadasdasd/1.0_32/plain_file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "flow-ota/ssasdadasdasd/1.0_32/qrcode-GradleXCodeMoreVariants-1.0_32.png").exists())
        assertTrue(new File(testProjectMoreVariants, "flow-ota/ssasdadasdasd/1.0_32/GradleXCodeMoreVariants/AnotherConfiguration/GradleXCodeMoreVariantsAnotherConfiguration-1.0_32.ipa").exists())
    }

    @Test
    void testBuildAndPrepareMoreVariantsMailMessageWithSimulators() {
        def p = new Properties()
        p.put('release.notes', 'some\nnotes')
        runGradleMoreVariants(p, 'cleanFlow', 'buildGradleXCodeMoreVariantsTestsDebug', 'prepareImageMontage', 'prepareAvailableArtifactsInfo', 'prepareMailMessage')
        def path = 'flow-ota/ssasdadasdasd/1.0_32'
        assertTrue(new File(testProjectMoreVariants, "$path/file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/icon.png").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/plain_file_index.html").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/qrcode-GradleXCodeMoreVariants-1.0_32.png").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsTests/Debug/GradleXCodeMoreVariantsTestsDebug-1.0_1-iPad-simulator-image.dmg").exists())
        assertTrue(new File(testProjectMoreVariants, "$path/GradleXCodeMoreVariantsTests/Debug/GradleXCodeMoreVariantsTestsDebug-1.0_1-iPhone-simulator-image.dmg").exists())
    }

    @Test
    void testBuildAndPrepareOneVariantMailMessage() {
        def p = new Properties()
        p.put('release.notes', 'some\nnotes')
        runGradleOneVariant(p, 'cleanFlow', 'buildAllDevice', 'prepareImageMontage', 'prepareMailMessage')
        assertTrue(new File(testProjectOneVariant, "flow-ota/GradleXCode/1.0_32/file_index.html").exists())
        assertTrue(new File(testProjectOneVariant, "flow-ota/GradleXCode/1.0_32/icon.png").exists())
        assertTrue(new File(testProjectOneVariant, "flow-ota/GradleXCode/1.0_32/index.html").exists())
        assertTrue(new File(testProjectOneVariant, "flow-ota/GradleXCode/1.0_32/plain_file_index.html").exists())
        assertTrue(new File(testProjectOneVariant, "flow-ota/GradleXCode/1.0_32/qrcode-GradleXCode-1.0_32.png").exists())
        assertTrue(new File(testProjectOneVariant, "flow-ota/GradleXCode/1.0_32/GradleXCode/BasicConfiguration/GradleXCode-1.0_32.ipa").exists())
    }

    @Test
    void testBuildNoVersion() {
        try {
            runGradleNoVersion('cleanFlow', 'buildGradleXCodeWithApphance')
            fail("There should be a version exception thrown!")
        } catch (BuildException e) {
            assertEquals('Verification error', e.cause.cause.cause.message)
        }
    }

    @Test
    void testBuildNoVersionString() {
        try {
            runGradleNoVersionString('cleanFlow', 'buildGradleXCodeWithApphance')
            fail("There should be a version exception thrown!")
        } catch (BuildException e) {
            assertEquals('Verification error', e.cause.cause.cause.message)
        }
    }

    @Test
    void testBuildAllSimulators() {
        runGradleMoreVariants('buildAllSimulator')
        def path = 'flow-ota/ssasdadasdasd/1.0_32/GradleXCodeMoreVariantsTests'

        ['AnotherConfiguration', 'BasicConfiguration', 'Debug', 'Release'].each {
            File fileIphone = new File(testProjectMoreVariants, "$path/$it/GradleXCodeMoreVariantsTests$it-1.0_1-iPhone-simulator-image.dmg")
            File fileIpad = new File(testProjectMoreVariants, "$path/$it/GradleXCodeMoreVariantsTests$it-1.0_1-iPad-simulator-image.dmg")
            assertTrue(fileIphone.exists())
            assertTrue(fileIphone.size() > 30000)
            assertTrue(fileIpad.exists())
            assertTrue(fileIpad.size() > 30000)
        }
    }

    @Test
    void testBuildSourcesZip() {
        runGradleMoreVariants('buildSourcesZip')
        File file = new File(testProjectMoreVariants, 'flow-tmp/GradleXCodeMoreVariants-1.0_32-src.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }

    @Ignore("ignored till apphance refactor")
    void testDefaultApphanceDependency() {
        Properties p = new Properties()
        runGradleWithProperties(p, gradleOneVariantConnection, 'clean', 'unlockKeyChain', 'build-GradleXCode-BasicConfiguration')
        def apphanceLib = new File("testProjects/ios/tmp-GradleXCode-GradleXCode-BasicConfiguration/Apphance-Pre-Production.framework")
        assertTrue(apphanceLib.exists())
        assertTrue(apphanceLib.list().length > 0)
    }

    @Ignore("ignored till apphance refactor")
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

    @Ignore("ignored till apphance refactor")
    void testCorrectApphanceDependency() {
        Properties p = new Properties()
        p.put('apphance.lib', 'com.apphance:ios.pre-production.armv7:1.8.2')
        runGradleWithProperties(p, gradleOneVariantConnection, 'clean', 'unlockKeyChain', 'build-GradleXCode-BasicConfiguration')
        def apphanceLib = new File("testProjects/ios/tmp-GradleXCode-GradleXCode-BasicConfiguration/Apphance-Pre-Production.framework")
        assertTrue(apphanceLib.exists())
    }
}
