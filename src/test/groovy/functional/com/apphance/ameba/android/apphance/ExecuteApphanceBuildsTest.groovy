package com.apphance.ameba.android.apphance

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import static com.apphance.ameba.configuration.ProjectConfiguration.TMP_DIR
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class ExecuteApphanceBuildsTest {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

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
        File tmpDir = new File(new File(directory, TMP_DIR), variant)
        return new File(tmpDir, 'src/com/apphance/amebaTest/android/MainApplication.java')
    }

    File getMainActivityFile(File directory, String variant) {
        File tmpDir = new File(new File(directory, TMP_DIR), variant)
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
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).run()
    }

    @Test
    public void testNoApphanceNoApplicationBuild() throws Exception {
        run(tNoApplication, 'cleanFlow', 'buildTestDebug', 'buildTestRelease')
        assertTrue(getMainActivityFile(tNoApplicationProject, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProject, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProject, 'TestDebug').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProject, 'TestDebug').exists())

        assertFalse(getMainActivityFile(tNoApplicationProject, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProject, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProject, 'TestRelease').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProject, 'TestRelease').exists())
    }

    @Test
    public void testNoApphanceApplicationBuild() throws Exception {
        run(tApplication, 'cleanFlow', 'buildTestDebug', 'buildTestRelease')
        assertFalse(getMainActivityFile(tApplicationProject, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tApplicationProject, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProject, 'TestDebug').text.contains('import android.util.Log'))

        assertTrue(getMainApplicationFile(tApplicationProject, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainApplicationFile(tApplicationProject, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProject, 'TestDebug').text.contains('import android.util.Log'))

        assertFalse(getMainActivityFile(tApplicationProject, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tApplicationProject, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProject, 'TestRelease').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tApplicationProject, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainApplicationFile(tApplicationProject, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProject, 'TestRelease').text.contains('import android.util.Log'))
    }

    @Test
    public void testNoApphanceNoApplicationBuildNoOnCreate() throws Exception {
        run(tNoApplicationNoOnCreate, 'cleanFlow', 'buildTestDebug', 'buildTestRelease')
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'TestDebug').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProjectNoOnCreate, 'TestDebug').exists())

        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProjectNoOnCreate, 'TestRelease').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tNoApplicationProjectNoOnCreate, 'TestRelease').exists())
    }

    @Test
    public void testNoApphanceApplicationBuildNoOnCreate() throws Exception {
        run(tApplicationNoOnCreate, 'cleanFlow', 'buildTestDebug', 'buildTestRelease')
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestDebug').text.contains('import android.util.Log'))

        assertTrue(getMainApplicationFile(tApplicationProjectNoOnCreate, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainApplicationFile(tApplicationProjectNoOnCreate, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestDebug').text.contains('import android.util.Log'))

        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestRelease').text.contains('import android.util.Log'))

        assertFalse(getMainApplicationFile(tApplicationProjectNoOnCreate, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainApplicationFile(tApplicationProjectNoOnCreate, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tApplicationProjectNoOnCreate, 'TestRelease').text.contains('import android.util.Log'))
    }

    @Test
    public void testNoApphanceNoApplicationDifferentBuild() throws Exception {
        run(tNoApplicationDifferentFormatting, 'cleanFlow', 'buildTestDebug', 'buildTestRelease')
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestDebug').text.contains('Apphance.startNewSession('))
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestDebug').text.contains('import com.apphance.android.Log'))
        assertFalse(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestDebug').text.contains('import android.util.Log'))
        assertFalse(getMainApplicationFile(tNoApplicationProjectDifferentFormatting, 'TestDebug').exists())
        assertFalse(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestRelease').text.contains('Apphance.startNewSession('))
        assertFalse(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestRelease').text.contains('import com.apphance.android.Log'))
        assertTrue(getMainActivityFile(tNoApplicationProjectDifferentFormatting, 'TestRelease').text.contains('import android.util.Log'))
        assertFalse(getMainApplicationFile(tNoApplicationProjectDifferentFormatting, 'TestRelease').exists())
    }

}
