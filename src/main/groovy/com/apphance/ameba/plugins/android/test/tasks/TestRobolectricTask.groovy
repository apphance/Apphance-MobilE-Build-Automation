package com.apphance.ameba.plugins.android.test.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static org.gradle.api.logging.Logging.getLogger

class TestRobolectricTask extends DefaultTask {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static String NAME = 'testRobolectric'
    String group = AMEBA_TEST
    String description = 'Runs Robolectric test on the project'

    private l = getLogger(getClass())
    private String robolectricPath = '/test/robolectric'

    @TaskAction
    void testRobolectric() {
        def path = new File(project.rootDir.path, robolectricPath)
        if (!(path.exists())) {
            l.warn("Running Robolectric test has failed. No valid tests present nor test project had been created under " +
                    "'${project.rootDir.path}/test/robolectric'. Run prepareRobolectric taks to (re)create unit test project.")
            return
        }

        ProjectConnection connection = getProjectConnection(project.rootDir, robolectricPath)
        try {
            BuildLauncher bl = connection.newBuild().forTasks('test');

            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            bl.setStandardOutput(baos)
            bl.setJvmArguments(GRADLE_DAEMON_ARGS)
            bl.run()
            String output = baos.toString('utf-8')
            println output
        } finally {
            connection.close()
        }
    }

    private ProjectConnection getProjectConnection(File baseFolder, String dirName) {
        def projectDir = new File(baseFolder, dirName)
        return GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }
}
