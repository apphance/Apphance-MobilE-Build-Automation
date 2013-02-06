package com.apphance.ameba.applyPlugins.ios

import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore

import static org.gradle.tooling.GradleConnector.newConnector
import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

/**
 * User: opal
 * Date: 06.02.2013
 * Time: 10:09
 */
class IOSApphancePluginUploadTest {
    static File project = new File('testProjects/ios/GradleXCodeWithApphance')
    static ProjectConnection projectConnection

    @BeforeClass
    static void beforeClass() {
        projectConnection = newConnector().forProjectDirectory(project).connect();
    }

    @AfterClass
    static public void afterClass() {
        projectConnection.close()
    }

    @Ignore
    /*
    This test is ignored as the testing scenario is very weak.
    For now apphance REST API isn't defined well.
    However, the test is left 'as is' because there was no apphance upload testing.
     */ public void testUpload() {
        try {
            projectConnection.newBuild().forTasks('clean', 'upload-GradleXCodeWithApphance-BasicConfiguration').run();
            assertTrue(true)
        } catch (e) {
            println "Test failed with msg: ${e.cause.cause.cause.message}"
            e.printStackTrace()
            fail()
        }
    }
}
