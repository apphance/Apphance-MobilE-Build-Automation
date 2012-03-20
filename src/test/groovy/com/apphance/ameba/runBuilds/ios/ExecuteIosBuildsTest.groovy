package com.apphance.ameba.runBuilds.ios;

import static org.junit.Assert.*


import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper



class ExecuteIosBuildsTest {

    File testProjectMoreVariants = new File("testProjects/ios-morevariants/GradleXCodeMoreVariants")
    File testProjectOneVariant = new File("testProjects/ios/GradleXCode")
    File templateFile = new File("templates/ios")

    protected void runGradleMoreVariants(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        try {
            def buildLauncher = connection.newBuild()
            buildLauncher.forTasks(tasks).run();
        } finally {
            connection.close();
        }
    }

    protected void runGradleWithProperties(Properties p, String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProjectMoreVariants).connect();
        try {
            def buildLauncher = connection.newBuild()
            def args = p.collect { property , value -> "-D${property}=${value}"}
            buildLauncher.setJvmArguments(args as String[])
            buildLauncher.forTasks(tasks).run()
        } finally {
            connection.close();
        }
    }

    protected void runGradleOneVariant(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProjectOneVariant).connect();
        try {
            connection.newBuild().forTasks(tasks).run();
        } finally {
            connection.close();
        }
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
