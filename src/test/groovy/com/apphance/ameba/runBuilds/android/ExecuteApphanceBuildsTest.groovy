package com.apphance.ameba.runBuilds.android;

import static org.junit.Assert.*

import java.io.File

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.apphance.ameba.ProjectHelper

class ExecuteApphanceBuildsTest {

    static File tApplicationProject = new File("testProjects/android-no-apphance-application")
    static File tNoApplicationProject = new File("testProjects/android-no-apphance-no-application")
    static File tApplicationProjectNoOnCreate = new File("testProjects/android-no-apphance-application-nooncreate")
    static File tNoApplicationProjectNoOnCreate = new File("testProjects/android-no-apphance-no-application-nooncreate")
    static ProjectConnection tApplicationProjectConnection
    static ProjectConnection tNoApplicationProjectConnection
    static ProjectConnection tApplicationProjectConnectionNoOnCreate
    static ProjectConnection tNoApplicationProjectConnectionNoOnCreate

     File getMainApplicationFile(File directory, String variant) {
        File tmpDir = new File(directory.parentFile, 'tmp-'  +  directory.name + '-' + variant)
        return new File(tmpDir, 'src/com/apphance/amebaTest/android/MainApplication.java')
    }

     File getMainActivityFile(File directory, String variant) {
        File tmpDir = new File(directory.parentFile, 'tmp-'  +  directory.name + '-' + variant)
        return new File(tmpDir, 'src/com/apphance/amebaTest/android/TestActivity.java')
    }

    @BeforeClass
    static void beforeClass() {
        tApplicationProjectConnection = GradleConnector.newConnector().forProjectDirectory(tApplicationProject).connect();
        tNoApplicationProjectConnection = GradleConnector.newConnector().forProjectDirectory(tNoApplicationProject).connect();
        tApplicationProjectConnectionNoOnCreate = GradleConnector.newConnector().forProjectDirectory(tApplicationProjectNoOnCreate).connect();
        tNoApplicationProjectConnectionNoOnCreate = GradleConnector.newConnector().forProjectDirectory(tNoApplicationProjectNoOnCreate).connect();
    }

    @AfterClass
    static void afterClass() {
        tApplicationProjectConnection.close()
        tNoApplicationProjectConnection.close()
        tApplicationProjectConnectionNoOnCreate.close()
        tNoApplicationProjectConnectionNoOnCreate.close()
    }

    protected void runNoApphanceApplicationGradle(String ... tasks) {
        def buildLauncher = tApplicationProjectConnection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run()
    }

    protected void runNoApphanceNoApplicationGradle(String ... tasks) {
        def buildLauncher = tNoApplicationProjectConnection.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run()
    }

    protected void runNoApphanceApplicationGradleNoOnCreate(String ... tasks) {
        def buildLauncher = tApplicationProjectConnectionNoOnCreate.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run()
    }

    protected void runNoApphanceNoApplicationGradleNoOnCreate(String ... tasks) {
        def buildLauncher = tNoApplicationProjectConnectionNoOnCreate.newBuild()
        buildLauncher.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run()
    }

    @Test
    public void testNoApphanceNoApplicationBuild() throws Exception {
        runNoApphanceNoApplicationGradle('buildAll')
        assertTrue(getMainActivityFile(tNoApplicationProject,'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProject,'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProject,'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProject,'Debug').exists())

        assertFalse(getMainActivityFile(tNoApplicationProject,'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProject,'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProject,'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProject,'Release').exists())
    }

        @Test
    public void testNoApphanceApplicationBuild() throws Exception {
        runNoApphanceApplicationGradle('buildAll')
        assertFalse(getMainActivityFile(tApplicationProject,'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tApplicationProject,'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProject,'Debug').text.contains('import android.util.Log'))

        assertTrue(getMainApplicationFile(tApplicationProject,'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainApplicationFile(tApplicationProject,'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProject,'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainActivityFile(tApplicationProject,'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tApplicationProject,'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProject,'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tApplicationProject,'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainApplicationFile(tApplicationProject,'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProject,'Release').text.contains('import android.util.Log'))
    }

        @Test
    public void testNoApphanceNoApplicationBuildNoOnCreate() throws Exception {
        runNoApphanceNoApplicationGradleNoOnCreate('buildAll')
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate,'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate,'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate,'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProjectNoOnCreate,'Debug').exists())

        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate,'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate,'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate,'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProjectNoOnCreate,'Release').exists())
    }

        @Test
    public void testNoApphanceApplicationBuildNoOnCreate() throws Exception {
        runNoApphanceApplicationGradleNoOnCreate('buildAll')
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate,'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate,'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate,'Debug').text.contains('import android.util.Log'))

        assertTrue(getMainApplicationFile(tApplicationProjectNoOnCreate,'Debug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainApplicationFile(tApplicationProjectNoOnCreate,'Debug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate,'Debug').text.contains('import android.util.Log'))

        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate,'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate,'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate,'Release').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tApplicationProjectNoOnCreate,'Release').text.contains('Apphance.startNewSession('))
        assertFalse(getMainApplicationFile(tApplicationProjectNoOnCreate,'Release').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate,'Release').text.contains('import android.util.Log'))
    }

}
