package com.apphance.ameba.runBuilds.ios;

import static org.junit.Assert.*

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test



class ExecuteIosBuildsTest {

    static File testProjectMoreVariants = new File("testProjects/ios-morevariants/GradleXCodeMoreVariants")
    static File testProjectOneVariant = new File("testProjects/ios/GradleXCode")
    static File templateFile = new File("templates/ios")
    static ProjectConnection connection
    static ProjectConnection gradleWithPropertiesConnection
    static ProjectConnection gradleOneVariantConnection

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        gradleWithPropertiesConnection = GradleConnector.newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        gradleOneVariantConnection = GradleConnector.newConnector().forProjectDirectory(testProjectOneVariant).connect();
    }

    @AfterClass
    static void afterClass() {
        connection.close()
        gradleWithPropertiesConnection.close()
        gradleOneVariantConnection.close()
    }

    protected void runGradleMoreVariants(String ... tasks) {
        def buildLauncher = connection.newBuild()
        buildLauncher.forTasks(tasks).run();
    }

    protected void runGradleWithProperties(Properties p, String ... tasks) {
        def buildLauncher = gradleWithPropertiesConnection.newBuild()
        def args = p.collect { property , value -> "-D${property}=${value}"}
        buildLauncher.setJvmArguments(args as String[])
        buildLauncher.forTasks(tasks).run()
    }

    protected void runGradleOneVariant(String ... tasks) {
        gradleOneVariantConnection.newBuild().forTasks(tasks).run();
    }

    @Test
    void testCleanCheckTests() {
        runGradleMoreVariants('clean','checkTests')
        assertFalse(new File(testProjectMoreVariants,"bin").exists())
        assertFalse(new File(testProjectMoreVariants,"build").exists())
    }

    @Test
    void testOta() {
        runGradleMoreVariants('cleanRelease')
        assertTrue(new File(testProjectMoreVariants,"ota").exists())
        assertEquals(0,new File(testProjectMoreVariants,"ota").listFiles().length)
        assertTrue(new File(testProjectMoreVariants,"tmp").exists())
        assertEquals(0,new File(testProjectMoreVariants,"tmp").listFiles().length)
    }

    @Test
    void testBuildOneVariant() {
        runGradleOneVariant('buildAll')
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
        runGradleMoreVariants('buildAll')
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
        // TODO: update after merging auto-IOS apphance
    }

    @Test
    void testBuildAndPrepareMoreVariantsMailMessage() {
        runGradleMoreVariants('cleanRelease', 'buildAll')
        runGradleMoreVariants('prepareImageMontage', 'prepareMailMessage')
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
        runGradleOneVariant('cleanRelease', 'buildAll')
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
    void testBuildSourcesZip() {
        runGradleMoreVariants('buildSourcesZip')
        File file = new File(testProjectMoreVariants, 'tmp/GradleXCodeMoreVariants-1.0-SNAPSHOT_32-src.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }
}
