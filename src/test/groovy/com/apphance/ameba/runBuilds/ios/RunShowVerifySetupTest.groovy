package com.apphance.ameba.runBuilds.ios;

import static org.junit.Assert.*;

import java.io.File;

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


class RunShowVerifySetupTest {
    static File testIosProject = new File("testProjects/ios/GradleXCode")
    static ProjectConnection connection

    @BeforeClass
    static beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect()
    }

    @AfterClass
    static afterClass() {
        connection.close()
    }

    String runTests(String ... tasks) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        BuildLauncher bl = connection.newBuild().forTasks(tasks);
        bl.setStandardOutput(os)
        bl.run()
        def res = os.toString("UTF-8")
        println res
        assertTrue(res.contains('BUILD SUCCESSFUL'))
        return res
    }

    @Test
    public void testShowSetup() {
        String res = runTests('showSetup')
        assertTrue(res.contains('# iOS properties'))
        assertTrue(res.contains('# iOS FoneMonkey properties'))
        assertTrue(res.contains('# iOS Framework properties'))
        assertTrue(res.contains('# Mercurial properties'))
        assertTrue(res.contains('# Release properties'))
    }

    @Test
    public void testVerifySetup() {
        String res = runTests('verifySetup')
        assertTrue(res.contains('GOOD!!! iOS properties'))
        assertTrue(res.contains('GOOD!!! iOS FoneMonkey properties'))
        assertTrue(res.contains('GOOD!!! iOS Framework properties'))
        assertTrue(res.contains('GOOD!!! Mercurial properties'))
        assertTrue(res.contains('GOOD!!! Release properties'))
    }
}
