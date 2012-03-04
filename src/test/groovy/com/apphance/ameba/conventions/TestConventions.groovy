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

    private static File documentationBase = new File("testProjects/documentation")
    private static File conventionsBase = new File("testProjects/conventions")
    private getProjectConnectionAndModel(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, "${dirName}")
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
        return [connection, connection.getModel(GradleProject.class)]
    }

    private getAllProjectsAndModels(String dirName) {
        def documentationConnection, documentationProject, conventionsConnection, conventionsProject
        (documentationConnection, documentationProject) = getProjectConnectionAndModel(documentationBase, dirName)
        (conventionsConnection, conventionsProject) = getProjectConnectionAndModel(conventionsBase, dirName)
        return [documentationConnection, documentationProject, conventionsConnection, conventionsProject]
    }

    @Test
    public void testAndroidAnalysisConvention() throws Exception {
        def documentationConnection, documentationProject, conventionsConnection, conventionsProject
        (documentationConnection, documentationProject, conventionsConnection, conventionsProject) =
            getAllProjectsAndModels("ameba-android-analysis")
         BuildLauncher bl = documentationConnection.newBuild().forTasks('showConventionAndroidAnalysis');
         ByteArrayOutputStream baos = new ByteArrayOutputStream()
         bl.setStandardOutput(baos)
         bl.run()
         String output = baos.toString('utf-8')
         println output
         assertTrue(output.contains('androidAnalysis {'))
    }
}
