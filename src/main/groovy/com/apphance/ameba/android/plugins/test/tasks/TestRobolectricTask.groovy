package com.apphance.ameba.android.plugins.test.tasks

import org.gradle.api.Project
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

class TestRobolectricTask {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    private String robolectricPath = '/test/robolectric'
    private Project project

    TestRobolectricTask(Project project) {
        this.project = project
    }

    void testRobolectric() {
        def path = new File(project.rootDir.path, robolectricPath)
        if (!(path.exists())) {
            println "Running Robolectric test has failed. No valid tests present nor test project had been created under " +
                    "'${project.rootDir.path}/test/robolectric'. Run prepareRobolectric taks to (re)create unit test project."
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
