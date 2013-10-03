package com.apphance.flow.android

import com.apphance.flow.TestUtils
import org.gradle.tooling.ProjectConnection
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory
import static org.gradle.tooling.GradleConnector.newConnector

@Mixin([TestUtils])
class ExecuteAndroidBuildsTest extends Specification {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static ProjectConnection connection
    File testTmpDir

    def setup() {
        testTmpDir = temporaryDir
        copyDirectory new File("projects/test/android/android-basic"), testTmpDir
        connection = newConnector().forProjectDirectory(testTmpDir).connect();
    }

    def cleanup() {
        connection.close()
    }

    def runGradle(String... tasks) {
        def buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).withArguments("-PflowProjectPath=${new File('.').absolutePath}").run()
    }

    def 'test analysis'() {
        when:
        runGradle('updateProject', 'check')

        then:
        true
        new File(testTmpDir, "build/reports/pmd/main.xml").exists()
        new File(testTmpDir, "build/reports/findbugs/main.xml").exists()
        new File(testTmpDir, 'build/reports/lint/report.html').exists()
        new File(testTmpDir, 'build/reports/lint/report.xml').exists()
        new File(testTmpDir, 'build/reports/cpd/cpd-result.xml').exists()
        new File(testTmpDir, 'build/reports/checkstyle/main.xml').exists()
    }
}
