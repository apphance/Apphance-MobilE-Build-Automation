package com.apphance.ameba.runBuilds.android;

import static org.junit.Assert.*;

import org.gradle.tooling.BuildException
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.Before
import org.junit.Test

class RunVerifyErrorsSetupTest {
    File testProject = new File("testProjects/android")
    File gradleProperties = new File(testProject,"gradle.properties")
    File gradlePropertiesOrig = new File(testProject,"gradle.properties.orig")

    @Before
    void before() {
        gradlePropertiesOrig.delete()
        gradlePropertiesOrig << gradleProperties.text
    }


    @After
    void after() {
        gradleProperties.delete()
        gradleProperties << gradlePropertiesOrig.text
    }

    String runTests(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            BuildLauncher bl = connection.newBuild().forTasks(tasks);
            bl.setStandardOutput(os)
            bl.run();
            def res = os.toString("UTF-8")
            println res
            assertFalse(res.contains('BUILD SUCCESSFUL'))
            return res
        } finally {
            connection.close();
        }
    }
    public void runErrorScenario(pattern, String replacement, String expected){
        gradleProperties.delete()
        String newText = gradlePropertiesOrig.text.split('\n')*.
                replaceFirst(pattern,replacement).join('\n')
        println newText
        gradleProperties << newText
        try {
            runTests('verifySetup')
        } catch (BuildException e) {
            println e.cause.cause.cause.message
            assertTrue(e.cause.cause.cause.message.contains(expected))
        }
    }

    @Test
    void testMainVariantFile() {
        runErrorScenario(/^(android\.mainVariant.*)=(.*)$/,'$1=/missingvariant', 'The main variant')
    }

    @Test
    void testMinSdkTargetFile() {
        runErrorScenario(/^(android\.minSdk\.target.*)=(.*)$/,'$1=/missingtarget', 'The min sdk target')
    }
}
