package com.apphance.ameba.runBuilds.android

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.android.AndroidManifestHelper
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*

class ExecuteAndroidBuildsTest {

    static File testProject = new File("testProjects/android")
    static File testNovariantsProject = new File("testProjects/android-novariants")
    static File testAndroidConventionProject = new File("testProjects/android-convention")
    static File testAndroidWrongConventionProject = new File("testProjects/android-convention-wrong-specs")
    static File templateFile = new File("templates/android")
    static ProjectConnection connection
    static ProjectConnection gradleWithPropertiesConnection
    static ProjectConnection gradleNoVariantsConnection
    static ProjectConnection testAndroidConventionConnection
    static ProjectConnection testAndroidWrongConventionConnection

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
        gradleWithPropertiesConnection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
        gradleNoVariantsConnection = GradleConnector.newConnector().forProjectDirectory(testNovariantsProject).connect();
        testAndroidConventionConnection = GradleConnector.newConnector().forProjectDirectory(testAndroidConventionProject).connect()
        testAndroidWrongConventionConnection = GradleConnector.newConnector().forProjectDirectory(testAndroidWrongConventionProject).connect()
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
        gradleWithPropertiesConnection.close()
        gradleNoVariantsConnection.close()
        testAndroidConventionConnection.close()
        testAndroidWrongConventionConnection.close()
    }

    protected void runGradle(String... tasks) {
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

    protected void runGradleNoVariants(String... tasks) {
        gradleNoVariantsConnection.newBuild().forTasks(tasks).run();
    }

    protected void runGradleAndroidAnalysis(String... tasks) {
        testAndroidConventionConnection.newBuild().forTasks(tasks).run();
    }

    protected void runGradleAndroidAnalysisWrongConvention(String... tasks) {
        testAndroidWrongConventionConnection.newBuild().forTasks(tasks).run();
    }

    @Test
    void testCleanCheckTests() {
        runGradle('updateProject', 'clean', 'checkTests')
        assertFalse(new File(testProject, "bin").exists())
        assertFalse(new File(testProject, "gen").exists())
        assertFalse(new File(testProject, "build").exists())
        assertFalse(new File(testProject, "tmp").exists())
    }

    @Test
    void testOta() {
        runGradle('updateProject', 'cleanRelease')
        assertTrue(new File(testProject, "ota").exists())
        assertEquals(0, new File(testProject, "ota").listFiles().length)
        assertTrue(new File(testProject, "tmp").exists())
        assertEquals(0, new File(testProject, "tmp").listFiles().length)
    }


    @Test
    void testBuildDebug() {
        runGradle('buildAllDebug')
        assertTrue(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }


    @Test
    void testBuildRelease() {
        runGradle('buildAllRelease')
        assertTrue(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildDebugNoVariant() {
        runGradleNoVariants('buildAllDebug')
        assertTrue(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-Debug-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-Debug-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-Debug-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildReleaseNoVariant() {
        runGradleNoVariants('buildAllRelease')
        assertTrue(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-Release-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-Debug-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-Debug-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }


    @Test
    void testJavadoc() {
        runGradle('updateProject', 'javadoc')
        assertTrue(new File(testProject, "build/docs").isDirectory())
        assertFalse(new File(testProject, "build/docs").listFiles().length == 0)
    }

    @Test
    void testUpdateProject() {
        File localProperties = new File(testProject, "local.properties")
        File localPropertiesSubproject = new File(testProject, "subproject/local.properties")
        File localPropertiesSubsubproject = new File(testProject, "subproject/subsubproject/local.properties")
        localProperties.delete()
        localPropertiesSubproject.delete()
        localPropertiesSubsubproject.delete()
        runGradle('updateProject')
        assertTrue(localProperties.exists())
        assertTrue(localPropertiesSubproject.exists())
        assertTrue(localPropertiesSubsubproject.exists())
    }


    @Test
    void testUpdateVersion() {
        AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
        ProjectConfiguration projectConf = new ProjectConfiguration()
        try {
            Properties p = new Properties()
            p.put('version.string', 'TEST_UPDATE')
            runGradleWithProperties(p, 'updateProject', 'updateVersion')
            manifestHelper.readVersion(new File("testProjects/android"), projectConf)
        } finally {
            manifestHelper.restoreOriginalManifest(new File("testProjects/android"))
        }
        assertEquals(43, projectConf.versionCode)
        assertEquals('TEST_UPDATE', projectConf.versionString)
    }


    @Test
    void testAnalysis() {
        File baseDir = new File(testProject, "build/analysis/")
        runGradle('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertTrue(new File(baseDir, "pmd-result.xml").exists())
    }

    private assertConfigSameAsBuild(File projectDirectory, String fileName) {
        File baseDir = new File(projectDirectory, "build/analysis/")
        File resourceDir = new File("src/main/resources/com/apphance/ameba/android/plugins/analysis/")
        File configBaseDir = new File(projectDirectory, "config/analysis/")
        assertEquals(new File(baseDir, fileName).text, new File(configBaseDir, fileName).text)
        assertFalse(new File(baseDir, fileName).text.equals(new File(resourceDir, fileName).text))
    }

    @Test
    void testAnalysisFromConfig() {
        File baseDir = new File(testNovariantsProject, "build/analysis/")
        File configBaseDir = new File(testNovariantsProject, "config/analysis/")
        runGradleNoVariants('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertConfigSameAsBuild(testNovariantsProject, "checkstyle-local-suppressions.xml")
        assertConfigSameAsBuild(testNovariantsProject, "checkstyle-suppressions.xml")
        assertConfigSameAsBuild(testNovariantsProject, "checkstyle.xml")
        assertConfigSameAsBuild(testNovariantsProject, "findbugs-exclude.xml")
        assertConfigSameAsBuild(testNovariantsProject, "pmd-rules.xml")
    }

    private assertRemoteSameAsBuild(File projectDirectory, File configDirectory, String fileName) {
        File baseDir = new File(projectDirectory, "build/analysis/")
        File resourceDir = new File("src/main/resources/com/apphance/ameba/android/plugins/analysis/")
        File configBaseDir = new File(configDirectory, "config/analysis/")
        assertEquals(new File(baseDir, fileName).text, new File(configBaseDir, fileName).text)
        assertFalse(new File(baseDir, fileName).text.equals(new File(resourceDir, fileName).text))
    }

    @Test
    void testAnalysisFromRemote() {
        File baseDir = new File(testAndroidConventionProject, "build/analysis/")
        runGradleAndroidAnalysis('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertRemoteSameAsBuild(testAndroidConventionProject, testNovariantsProject, "checkstyle-local-suppressions.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNovariantsProject, "checkstyle-suppressions.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNovariantsProject, "checkstyle.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNovariantsProject, "findbugs-exclude.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNovariantsProject, "pmd-rules.xml")
    }

    @Test
    void testAnalysisFromRemoteWrongConvention() {
        File baseDir = new File(testAndroidWrongConventionProject, "build/analysis/")
        runGradleAndroidAnalysisWrongConvention('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertConfigSameAsBuild(testAndroidWrongConventionProject, "checkstyle.xml")
        assertConfigSameAsBuild(testAndroidWrongConventionProject, "findbugs-exclude.xml")
        assertConfigSameAsBuild(testAndroidWrongConventionProject, "pmd-rules.xml")
    }


    @Test
    void testAnalysisAfterClean() {
        runGradle('clean', 'updateProject', 'analysis')
        assertTrue(new File(testProject, "build/analysis/checkstyle-report.xml").exists())
        assertTrue(new File(testProject, "build/analysis/cpd-result.xml").exists())
        assertTrue(new File(testProject, "build/analysis/findbugs-result.xml").exists())
        assertTrue(new File(testProject, "build/analysis/pmd-result.xml").exists())
    }

    @Test
    void testBuildAndPrepareVariantedMailMessage() {
        runGradle('cleanRelease', 'updateProject', 'buildAll')
        runGradle('prepareImageMontage', 'prepareMailMessage')
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/file_index.html").exists())
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/icon.png").exists())
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/index.html").exists())
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/plain_file_index.html").exists())
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/qrcode-TestAndroidProject-1.0.1-SNAPSHOT_42.png").exists())
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-1.0.1-SNAPSHOT_42.apk").exists())
        assertTrue(new File(testProject, "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildAndPrepareNonVariantedMailMessage() {
        runGradleNoVariants('cleanRelease', 'updateProject', 'buildAll')
        runGradleNoVariants('prepareImageMontage', 'prepareMailMessage')
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/file_index.html").exists())
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/icon.png").exists())
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/index.html").exists())
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/plain_file_index.html").exists())
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/qrcode-TestAndroidProject-1.0.1-SNAPSHOT_42.png").exists())
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-Debug-1.0.1-SNAPSHOT_42.apk").exists())
        assertTrue(new File(testNovariantsProject, "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-Release-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildDocumentationZip() {
        runGradle('buildDocumentationZip')
        File file = new File('testProjects/android/tmp/TestAndroidProject-1.0.1-SNAPSHOT_42-doc.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }

    @Test
    void testBuildSourcesZip() {
        runGradle('buildSourcesZip')
        File file = new File('testProjects/android/tmp/TestAndroidProject-1.0.1-SNAPSHOT_42-src.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }

    @Test
    void testRunCleanAVD() {
        runGradle('cleanAVD')
        File avdsDirectory = new File('testProjects/android/avds')
        assertFalse(avdsDirectory.exists())
    }

    @Test
    void testRunAndroidCreateAVD() {
        runGradle('cleanAVD', 'createAVD')
        def files = [
                'config.ini',
                'sdcard.img',
                'snapshots.img',
                'userdata.img'
        ]
        File avdsDirectory = new File('testProjects/android/avds')
        assertTrue(avdsDirectory.exists())
        files.each {
            assertTrue(it, new File(avdsDirectory, it).exists())
        }
    }
}
