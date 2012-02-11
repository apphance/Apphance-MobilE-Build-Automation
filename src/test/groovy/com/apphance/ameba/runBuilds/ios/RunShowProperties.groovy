package com.apphance.ameba.runBuilds.ios;

import static org.junit.Assert.*;

import java.io.File;

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test;

class RunShowProperties {
    File testIosProject = new File("testProjects/ios/GradleXCode")

    void runTests(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            BuildLauncher bl = connection.newBuild().forTasks(tasks);
            bl.setStandardOutput(os)
            bl.run();
            def res = os.toString("UTF-8")
            println res
        } finally {
            connection.close();
        }
    }

    @Test
    public void testShowSetup() {
        runTests('showSetup')
    }
}
