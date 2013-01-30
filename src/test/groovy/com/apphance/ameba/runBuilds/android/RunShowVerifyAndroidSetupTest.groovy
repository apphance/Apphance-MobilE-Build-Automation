package com.apphance.ameba.runBuilds.android

import com.apphance.ameba.ProjectHelper
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertTrue


class RunShowVerifyAndroidSetupTest {
    static File testIosProject = new File("testProjects/android/android-basic")
    static ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect();

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect();
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
    }

    String runTests(String... tasks) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        BuildLauncher bl = connection.newBuild().forTasks(tasks);
        bl.setStandardOutput(os)
        bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        bl.run()
        def res = os.toString("UTF-8")
        println res
        assertTrue(res.contains('BUILD SUCCESSFUL'))
        return res
    }

    @Test
    public void testShowSetup() {
        String res = runTests('showSetup')
        assertTrue(res.contains('# Android properties'))
        assertTrue(res.contains('# Android jar library properties'))
        assertTrue(res.contains('# Android test properties'))
        assertTrue(res.contains('# Mercurial properties'))
        assertTrue(res.contains('# Release properties'))
    }

    @Test
    public void testVerifySetup() {
        String res = runTests('verifySetup')
        assertTrue(res.contains('GOOD!!! Android properties'))
        assertTrue(res.contains('GOOD!!! Android jar library properties'))
        assertTrue(res.contains('GOOD!!! Android test properties'))
        assertTrue(res.contains('GOOD!!! Mercurial properties'))
        assertTrue(res.contains('GOOD!!! Release properties'))
    }
}
