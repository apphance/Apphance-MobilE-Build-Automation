package com.apphance.ameba.android

import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import static org.gradle.tooling.GradleConnector.newConnector
import static org.junit.Assert.*

class ExecuteAndroidBuildsTest {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static File testProject = new File("testProjects/android/android-basic")
    static File testNoVariantsProject = new File("testProjects/android/android-novariants")
    static File testAndroidConventionProject = new File("testProjects/android/android-convention")
    static File testAndroidWrongConventionProject = new File("testProjects/android/android-convention-wrong-specs")
    static File testAndroidNoApphanceApplication = new File("testProjects/android/android-no-apphance-application")
    static ProjectConnection testProjectConnection
    static ProjectConnection gradleWithPropertiesConnection
    static ProjectConnection gradleNoVariantsConnection
    static ProjectConnection testAndroidConventionConnection
    static ProjectConnection testAndroidWrongConventionConnection
    static ProjectConnection testAndroidNoApphanceApplicationConnection

    @BeforeClass
    static void beforeClass() {
        testProjectConnection = newConnector().forProjectDirectory(testProject).connect();
        gradleWithPropertiesConnection = newConnector().forProjectDirectory(testProject).connect();
        gradleNoVariantsConnection = newConnector().forProjectDirectory(testNoVariantsProject).connect();
        testAndroidConventionConnection = newConnector().forProjectDirectory(testAndroidConventionProject).connect()
        testAndroidWrongConventionConnection = newConnector().forProjectDirectory(testAndroidWrongConventionProject).connect()
        testAndroidNoApphanceApplicationConnection = newConnector().forProjectDirectory(testAndroidNoApphanceApplication).connect()
    }

    @AfterClass
    static public void afterClass() {
        testProjectConnection.close()
        gradleWithPropertiesConnection.close()
        gradleNoVariantsConnection.close()
        testAndroidConventionConnection.close()
        testAndroidWrongConventionConnection.close()
    }

    protected void runGradle(OutputStream output = null, String... tasks) {
        def buildLauncher = testProjectConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        if (output)
            buildLauncher.standardOutput = output
        buildLauncher.forTasks(tasks).run()

    }

    protected void runGradleWithProperties(Properties p, ProjectConnection pc = gradleWithPropertiesConnection, String... tasks) {
        def buildLauncher = pc.newBuild()
        def args = p.collect { property, value -> "-D${property}=${value}" }
        GRADLE_DAEMON_ARGS.each { args << it }
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
    void testAnalysis() {
        File baseDir = new File(testProject, "build/analysis/")
        runGradle('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertTrue(new File(baseDir, "pmd-result.xml").exists())
    }

    @Test
    void testAnalysisFromConfig() {
        File baseDir = new File(testNoVariantsProject, "build/analysis/")
        runGradleNoVariants('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertConfigSameAsBuild(testNoVariantsProject, "checkstyle-local-suppressions.xml")
        assertConfigSameAsBuild(testNoVariantsProject, "checkstyle-suppressions.xml")
        assertConfigSameAsBuild(testNoVariantsProject, "checkstyle.xml")
        assertConfigSameAsBuild(testNoVariantsProject, "findbugs-exclude.xml")
        assertConfigSameAsBuild(testNoVariantsProject, "pmd-rules.xml")
    }

    @Test
    void testAnalysisFromRemote() {
        File baseDir = new File(testAndroidConventionProject, "build/analysis/")
        runGradleAndroidAnalysis('updateProject', 'analysis')
        assertTrue(new File(baseDir, "checkstyle-report.xml").exists())
        assertTrue(new File(baseDir, "cpd-result.xml").exists())
        assertTrue(new File(baseDir, "findbugs-result.xml").exists())
        assertRemoteSameAsBuild(testAndroidConventionProject, testNoVariantsProject, "checkstyle-local-suppressions.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNoVariantsProject, "checkstyle-suppressions.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNoVariantsProject, "checkstyle.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNoVariantsProject, "findbugs-exclude.xml")
        assertRemoteSameAsBuild(testAndroidConventionProject, testNoVariantsProject, "pmd-rules.xml")
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

    private assertConfigSameAsBuild(File projectDirectory, String fileName) {
        File baseDir = new File(projectDirectory, "build/analysis/")
        File resourceDir = new File("src/main/resources/com/apphance/ameba/plugins/android/analysis/tasks")
        File configBaseDir = new File(projectDirectory, "config/analysis/")
        assertEquals(new File(baseDir, fileName).text, new File(configBaseDir, fileName).text)
        assertFalse(new File(baseDir, fileName).text.equals(new File(resourceDir, fileName).text))
    }

    private assertRemoteSameAsBuild(File projectDirectory, File configDirectory, String fileName) {
        File baseDir = new File(projectDirectory, "build/analysis/")
        File resourceDir = new File("src/main/resources/com/apphance/ameba/plugins/android/analysis/tasks")
        File configBaseDir = new File(configDirectory, "config/analysis/")
        assertEquals(new File(baseDir, fileName).text, new File(configBaseDir, fileName).text)
        assertFalse(new File(baseDir, fileName).text.equals(new File(resourceDir, fileName).text))
    }

    @Test
    void testAnalysisAfterClean() {
        runGradle('cleanFlow', 'updateProject', 'analysis')
        assertTrue(new File(testProject, "build/analysis/checkstyle-report.xml").exists())
        assertTrue(new File(testProject, "build/analysis/cpd-result.xml").exists())
        assertTrue(new File(testProject, "build/analysis/findbugs-result.xml").exists())
        assertTrue(new File(testProject, "build/analysis/pmd-result.xml").exists())
    }

    @Ignore('to be used after apphance rewritten')
    void testDefaultApphanceDependency() {
//        AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
//        ProjectConfiguration projectConf = new ProjectConfiguration()
//        try {
//            Properties p = new Properties()
//            runGradleWithProperties(p, testAndroidNoApphanceApplicationConnection, 'clean', 'buildAllDebug')
//            projectConf.updateVersionDetails(manifestHelper.readVersion(testAndroidNoApphanceApplication))
//        } finally {
//            manifestHelper.restoreOriginalManifest(testAndroidNoApphanceApplication)
//        }
//        def androidLib = new File("testProjects/android/android-no-apphance-application/${TMP_DIR}/TestDebug/libs/android.pre-production-1.8.2.jar")
//        assertTrue(androidLib.exists())
//        assertEquals('android.pre-production-1.8.2.jar', androidLib.name)
    }

    @Ignore('to be used after apphance rewritten')
    void testCorrectApphanceDependencyFromProperty() {
        AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
//        ProjectConfiguration projectConf = new ProjectConfiguration()
//        try {
//            Properties p = new Properties()
//            p.put('apphance.lib', "com.apphance:android.production:1.8.2")
//            runGradleWithProperties(p, testAndroidNoApphanceApplicationConnection, 'clean', 'buildAllDebug')
//            projectConf.updateVersionDetails(manifestHelper.readVersion(testAndroidNoApphanceApplication))
//        } finally {
//            manifestHelper.restoreOriginalManifest(testAndroidNoApphanceApplication)
//        }
//        def androidLib = new File("testProjects/android/android-no-apphance-application/MarketDebug/libs/android.production-1.8.2.jar")
//        assertTrue(androidLib.exists())
//        assertEquals('android.production-1.8.2.jar', androidLib.name)
    }

    @Ignore('to be used after apphance rewritten')
    void testIncorrectApphanceDependencyFromProperty() {
        AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
//        ProjectConfiguration projectConf = new ProjectConfiguration()
//        try {
//            Properties p = new Properties()
//            p.put('apphance.lib', "com.apphanc:android.production:1.8")
//            runGradleWithProperties(p, testAndroidNoApphanceApplicationConnection, 'clean', 'buildAllDebug')
//            projectConf.updateVersionDetails(manifestHelper.readVersion(testAndroidNoApphanceApplication))
//        } catch (Exception e) {
//
//            def c = e.cause.cause.cause
//            assertEquals("Error while resolving dependency: 'com.apphanc:android.production:1.8'", c.message)
//
//        } finally {
//            manifestHelper.restoreOriginalManifest(testAndroidNoApphanceApplication)
//        }
//        def androidLibsDir = new File("testProjects/android/tmp-android-no-apphance-application-Debug/libs/")
//        assertTrue(androidLibsDir.exists())
//        assertTrue(androidLibsDir.list().length == 0)
    }
}
