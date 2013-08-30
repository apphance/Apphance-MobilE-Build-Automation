package com.apphance.flow.android.apphance

import com.apphance.flow.TestUtils
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static org.apache.commons.io.FileUtils.copyDirectory

@Mixin(TestUtils)
class ExecuteApphanceBuildsTest extends Specification {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled', '-XX:+CMSPermGenSweepingEnabled',
            '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static File projectDir
    static ProjectConnection connection

    void setupSpec() {
        projectDir = temporaryDir
        copyDirectory(new File("testProjects/android/android-no-apphance-application"), projectDir)
        connection = GradleConnector.newConnector().forProjectDirectory(projectDir).connect();
    }

    void cleanupSpec() {
        connection.close()
    }

    protected void run(ProjectConnection projectConnection, String... tasks) {
        def buildLauncher = projectConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.forTasks(tasks).withArguments("-PflowProjectPath=${new File('.').absolutePath}").run()
    }

    def 'test apphance addition'() {
        given:
        run(connection, 'cleanFlow', 'buildTestDebug', 'buildMarketDebug')
        File testDebugTestActivity = new File(projectDir, "$TMP_DIR/TestDebug/src/com/apphance/flowTest/android/TestActivity.java")
        File marketDebugTestActivity = new File(projectDir, "$TMP_DIR/MarketDebug/src/com/apphance/flowTest/android/TestActivity.java")
        def apphanceLines = ['Apphance.startNewSession(', 'import com.apphance.android.Log', 'Apphance.onStart(this);']

        expect:
        apphanceLines.every { testDebugTestActivity.text.contains it }
        apphanceLines.every { !marketDebugTestActivity.text.contains(it) }

        new File(projectDir, "flow-ota/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-MarketDebug-1.0.1_42.apk").exists()
        new File(projectDir, "flow-ota/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-1.0.1_42.apk").exists()
    }
}
