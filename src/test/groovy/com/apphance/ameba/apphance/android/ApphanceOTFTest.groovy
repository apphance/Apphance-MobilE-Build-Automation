package com.apphance.ameba.apphance.android

import com.apphance.ameba.ProjectHelper
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertTrue

class ApphanceOTFTest {

    static File testNovariantsProject = new File("testProjects/android-novariants")
    static ProjectConnection connection

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testNovariantsProject).connect();
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
    }


    protected void runGradleNoVariants(String... tasks) {
        def launcher = connection.newBuild().forTasks(tasks)
        launcher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        launcher.run()
    }

    @Test
    void testAddAmeba() {
        File mainActivityFile = new File(testNovariantsProject, "src/com/apphance/amebaTest/android/TestActivity.java")
        File tmpCopy = new File("tmpCopy")
        tmpCopy.delete()
        tmpCopy << mainActivityFile.getText()

        runGradleNoVariants('updateProject', 'cleanRelease', 'buildDebug')
        assertTrue(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-Debug-1.0.1-SNAPSHOT_42.apk").exists())

        mainActivityFile.delete()
        mainActivityFile << tmpCopy.getText()
    }
}
