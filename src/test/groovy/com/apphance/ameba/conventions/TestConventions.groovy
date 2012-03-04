package com.apphance.ameba.conventions;

import static org.junit.Assert.*;

import java.io.File;

import org.gradle.api.internal.artifacts.ivyservice.projectmodule.ProjectModuleRegistry;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.junit.Test;

class TestConventions {
    private static File conventionsBase = new File("testProjects/conventions")

    private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, dirName)
        return  GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    @Test
    public void testAndroidAnalysisConvention() throws Exception {
         ProjectConnection connection = getProjectConnection("ameba-android-analysis")
         BuildLauncher bl = connection.newBuild().forTasks('showConventionAndroidAnalysis');
         ByteArrayOutputStream baos = new ByteArrayOutputStream()
         bl.setStandardOutput(baos)
         bl.run()
         String output = baos.toString('utf-8')
         println output
         assertTrue(output.contains('androidAnalysis {'))
    }
}
