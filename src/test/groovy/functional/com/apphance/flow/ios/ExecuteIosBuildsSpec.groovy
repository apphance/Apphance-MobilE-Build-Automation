package com.apphance.flow.ios

import org.gradle.tooling.ProjectConnection
import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.tooling.GradleConnector.newConnector

class ExecuteIosBuildsSpec extends Specification {

    @Shared List<String> GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    @Shared File testProject = new File('testProjects/ios/GradleXCode')
    @Shared ProjectConnection testProjectConnection

    def setupSpec() {
        testProjectConnection = newConnector().forProjectDirectory(testProject).connect()
    }

    def cleanupSpec() {
        testProjectConnection.close()
    }

    def 'single device variant is archived'() {
        when:
        runGradleOneVariant('cleanFlow', 'archiveGradleXCode')

        then:
        noExceptionThrown()

        then:
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCode/'
        new File(testProject, "$path/GradleXCode-1.0_32.ipa").exists()
        new File(testProject, "$path/GradleXCode-1.0_32.mobileprovision").exists()
        new File(testProject, "$path/GradleXCode-1.0_32.zip").exists()
        new File(testProject, "$path/GradleXCode-1.0_32_xcarchive.zip").exists()
        new File(testProject, "$path/GradleXCode-1.0_32_dSYM.zip").exists()
        new File(testProject, "$path/manifest.plist").exists()
        new File(testProject, "$path/GradleXCode-1.0_32_ahSYM").exists()
        new File(testProject, "$path/GradleXCode-1.0_32_ahSYM").listFiles().size() > 0
    }

    def 'single device variant with apphance is archived'() {
        when:
        runGradleOneVariant('cleanFlow', 'archiveGradleXCodeWithApphance')

        then:
        noExceptionThrown()

        then:
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCodeWithApphance/'
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32.ipa").exists()
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32.mobileprovision").exists()
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32.zip").exists()
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32_xcarchive.zip").exists()
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32_dSYM.zip").exists()
        new File(testProject, "$path/manifest.plist").exists()
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32_ahSYM").exists()
        new File(testProject, "$path/GradleXCodeWithApphance-1.0_32_ahSYM").listFiles().size() > 0
        new File(testProject, "flow-tmp/GradleXCodeWithApphance/Apphance-Pre-Production.framework").exists()
    }

    def 'exception is raised on test fail during build'() {
        when:
        runGradleOneVariant('cleanFlow', 'testGradleXCodeWithSpace')

        then:
        def e = thrown(Exception)
        def msg = e.cause.cause.cause.message
        msg.startsWith("Error while executing tests for variant: GradleXCode With Space, target: GradleXCodeFailingTests, configuration Debug. For further details investigate test results:")
        msg.endsWith("test-GradleXCode With Space-GradleXCodeFailingTests.xml")
    }

    def 'single simulator variant is archived'() {
        when:
        runGradleOneVariant('cleanFlow', 'archiveGradleXCodeSimulator')

        then:
        noExceptionThrown()

        then:
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCodeSimulator'
        def iPadSim = new File(testProject, "$path/GradleXCodeSimulator-1.0_32-iPad-sim-img.dmg")
        def iPhoneSim = new File(testProject, "$path/GradleXCodeSimulator-1.0_32-iPhone-sim-img.dmg")
        iPhoneSim.exists()
        iPadSim.exists()
        iPhoneSim.size() > 30000
        iPadSim.size() > 30000
    }

    def runGradleOneVariant(String... tasks) {
        def buildLauncher = testProjectConnection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run();
    }
}
