package com.apphance.ameba.runBuilds.android

import com.apphance.ameba.ProjectHelper
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ExecuteApphanceBuildsTest {

    static File tApplicationProject = new File("testProjects/android/android-no-apphance-application")
    static File tNoApplicationProject = new File("testProjects/android/android-no-apphance-no-application")
    static File tApplicationProjectNoOnCreate = new File("testProjects/android/android-no-apphance-application-nooncreate")
    static File tNoApplicationProjectNoOnCreate = new File("testProjects/android/android-no-apphance-no-application-nooncreate")
    static File tNoApplicationProjectDifferentFormatting = new File("testProjects/android/android-no-apphance-no-application-different-formatting")
    static ProjectConnection tApplication
    static ProjectConnection tNoApplication
    static ProjectConnection tNoApplicationDifferentFormatting
    static ProjectConnection tApplicationNoOnCreate
    static ProjectConnection tNoApplicationNoOnCreate

    File getMainApplicationFile(File directory, String variant) {
        File tmpDir = new File(directory.parentFile, 'tmp-' + directory.name + '-' + variant)
        return new File(tmpDir, 'src/com/apphance/amebaTest/android/MainApplication.java')
    }

    File getMainActivityFile(File directory, String variant) {
        File tmpDir = new File(directory.parentFile, 'tmp-' + directory.name + '-' + variant)
        return new File(tmpDir, 'src/com/apphance/amebaTest/android/TestActivity.java')
    }

    @BeforeClass
    static void beforeClass() {
        tApplication = GradleConnector.newConnector().forProjectDirectory(tApplicationProject).connect();
        tNoApplication = GradleConnector.newConnector().forProjectDirectory(tNoApplicationProject).connect();
        tNoApplicationDifferentFormatting = GradleConnector.newConnector().forProjectDirectory(tNoApplicationProjectDifferentFormatting).connect();
        tApplicationNoOnCreate = GradleConnector.newConnector().forProjectDirectory(tApplicationProjectNoOnCreate).connect();
        tNoApplicationNoOnCreate = GradleConnector.newConnector().forProjectDirectory(tNoApplicationProjectNoOnCreate).connect();
    }

    @AfterClass
    static public void afterClass() {
        tApplication.close()
        tNoApplication.close()
        tNoApplicationDifferentFormatting.close()
        tApplicationNoOnCreate.close()
        tNoApplicationNoOnCreate.close()
    }

    protected void run(ProjectConnection projectConnection, String... tasks) {
        def buildLauncher = projectConnection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run()
    }

    @Test
    public void testNoApphanceNoApplicationBuild() throws Exception {
        run(tNoApplication, 'buildAll')
        assertTrue(getMainActivityFile(tNoApplicationProject, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProject, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProject, 'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProject, 'Debug').exists())

        assertFalse(getMainActivityFile(tNoApplicationProject, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProject, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProject, 'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProject, 'Release').exists())
    }

    @Test
    public void testNoApphanceApplicationBuild() throws Exception {
        run(tApplication, 'buildAll')
        assertFalse(getMainActivityFile(tApplicationProject, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tApplicationProject, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProject, 'Debug').text.contains('import android.util.Log'))

        assertTrue(getMainApplicationFile(tApplicationProject, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainApplicationFile(tApplicationProject, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProject, 'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainActivityFile(tApplicationProject, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tApplicationProject, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProject, 'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tApplicationProject, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainApplicationFile(tApplicationProject, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProject, 'Release').text.contains('import android.util.Log'))
    }

    @Test
    public void testNoApphanceNoApplicationBuildNoOnCreate() throws Exception {
        run(tNoApplicationNoOnCreate, 'buildAll')
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProjectNoOnCreate, 'Debug').exists())

        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProjectNoOnCreate, 'Release').exists())
    }

    @Test
    public void testNoApphanceApplicationBuildNoOnCreate() throws Exception {
        run(tApplicationNoOnCreate, 'buildAll')
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'Debug').text.contains('import android.util.Log'))

        assertTrue(getMainApplicationFile(tApplicationProjectNoOnCreate, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainApplicationFile(tApplicationProjectNoOnCreate, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate, 'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tApplicationProjectNoOnCreate, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainApplicationFile(tApplicationProjectNoOnCreate, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate, 'Release').text.contains('import android.util.Log'))
    }

    @Test
    public void testNoApphanceNoApplicationDifferentBuild() throws Exception {
        run(tNoApplicationDifferentFormatting, 'buildAll')
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Debug').text.contains('import android.util.Log'))
        assertFalse(getMainApplicationFile(tNoApplicationProjectDifferentFormatting, 'Debug').exists())
        assertFalse(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'Release').text.contains('import android.util.Log'))
        assertFalse(getMainApplicationFile(tNoApplicationProjectDifferentFormatting, 'Release').exists())
    }

}
