package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class TestRobolectricTask extends DefaultTask {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static String NAME = 'testRobolectric'
    String group = FLOW_TEST
    String description = 'Runs Robolectric test on the project'

    @Inject AndroidConfiguration conf

    private String robolectricPath = '/test/robolectric'

    @TaskAction
    void testRobolectric() {
        def path = new File(conf.rootDir.path, robolectricPath)
        if (!(path.exists())) {
            logger.warn("Running Robolectric test has failed. No valid tests present nor test project had been created under " +
                    "'${conf.rootDir.path}/test/robolectric'. Run prepareRobolectric task to (re)create unit test project.")
            return
        }

        ProjectConnection connection = getProjectConnection(conf.rootDir, robolectricPath)
        try {
            BuildLauncher bl = connection.newBuild().forTasks('clean', 'test');

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
