package com.apphance.flow.doc

import com.apphance.flow.TestUtils
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.ProjectConnection
import spock.lang.Specification
import spock.lang.Unroll

import static org.apache.commons.io.FileUtils.copyDirectory
import static org.gradle.tooling.GradleConnector.newConnector

@Mixin([TestUtils])
class BuildDocSpec extends Specification {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled', '-XX:+CMSPermGenSweepingEnabled',
            '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    static ProjectConnection connection
    static def outFileName = 'build/doc.json'
    File testTmpDir
    File output = tempFile
    BuildLauncher buildLauncher

    def setup() {
        testTmpDir = temporaryDir
        copyDirectory new File("demo/android/android-basic"), testTmpDir
        connection = newConnector().forProjectDirectory(testTmpDir).connect()
        buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
    }

    def cleanup() {
        connection.close()
    }

    @Unroll
    def 'test build without documentation'() {
        when:
        buildLauncher.withArguments([additionalArgs, "-PflowProjectPath=${new File('.').absolutePath}"].flatten() as String[]).run()

        then:
        !new File(testTmpDir, outFileName).exists()

        where:
        additionalArgs << [[], "-PoutDocFile=$outFileName"]
    }

    def 'test build documentation'() {
        when:
        buildLauncher.withArguments("-Pflow.doc.mode=true", "-Pflow.doc.file=$outFileName", '-i', "-PflowProjectPath=${new File('.').absolutePath}").run()

        def outFile = new File(testTmpDir, outFileName)

        then:
        outFile.exists()
        outFile.size() > 0
    }
}
