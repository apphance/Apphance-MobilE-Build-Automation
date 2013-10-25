package com.apphance.flow.docs

import com.apphance.flow.TestUtils
import groovy.json.JsonSlurper
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.ProjectConnection
import spock.lang.Specification

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
        copyDirectory new File("projects/doc/android"), testTmpDir
        connection = newConnector().forProjectDirectory(testTmpDir).connect()
        buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
    }

    def cleanup() {
        connection.close()
    }

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
        buildLauncher.withArguments("-PdocMode=true", "-PdocFile=$outFileName", '-i', "-PflowProjectPath=${new File('.').absolutePath}").run()

        def outFile = new File(testTmpDir, outFileName)

        then:
        outFile.exists()
        outFile.size() > 0
        new JsonSlurper().parseText(outFile.text)
    }
}
