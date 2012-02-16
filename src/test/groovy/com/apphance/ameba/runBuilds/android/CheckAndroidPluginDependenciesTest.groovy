package com.apphance.ameba.runBuilds.android;

import static org.junit.Assert.*;

import org.gradle.tooling.BuildException;
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Test;


class CheckAndroidPluginDependenciesTest {
    File testProject = new File("testProjects/test-dependencies")
    File gradleBuild = new File(testProject,"build.gradle")

    @Before
    void before() {
        gradleBuild.delete()
    }


    @After
    void after() {
        gradleBuild.delete()
    }

    String runTests(File gradleBuildToCopy, String expected, String ... tasks) {
        gradleBuild << gradleBuildToCopy.text
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        try {
            BuildLauncher bl = connection.newBuild().forTasks(tasks);
            bl.setStandardOutput(os)
            bl.run();
            def res = os.toString("UTF-8")
            println res
            assertFalse(res.contains('BUILD SUCCESSFUL'))
            return res
        } catch (BuildException e) {
            def res = os.toString("UTF-8")
            def msg = e.cause.cause.cause.message
            println msg
            assertTrue(msg.contains(expected))
            println res
            return res
        } finally {
            connection.close();
        }
    }

    @Test
    public void testBuildAnalysisDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-analysis.gradle'), 'AndroidPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testAndroidPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-plugin.gradle'), 'ProjectConfigurationPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testIosPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'ios-plugin.gradle'), 'ProjectConfigurationPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testAndroidApphancePluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-apphance.gradle'), 'AndroidPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testAndroidJarlibraryPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-jarlibrary.gradle'), 'AndroidPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testAndroidReleaseAndroidPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-release-android.gradle'), 'AndroidPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testAndroidReleaseCommonPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-release-common.gradle'), 'ReleasePlugin has not been loaded yet')
        println res
    }

    @Test
    public void testCommonReleasePluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'project-release.gradle'), 'None of the plugins')
        println res
    }

    @Test
    public void testCommonReleaseVcsPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'project-release-vcs.gradle'), 'None of the plugins')
        println res
    }

    @Test
    public void testAndroidTestPluginDependencies() throws Exception {
        String res = runTests(new File(testProject, 'android-test.gradle'), 'AndroidPlugin has not been loaded yet')
        println res
    }

    @Test
    public void testMercurialGitDependencies() throws Exception {
        String res = runTests(new File(testProject, 'project-mercurial-git.gradle'), 'There is more than one plugin loaded from the list')
        println res
    }
}
