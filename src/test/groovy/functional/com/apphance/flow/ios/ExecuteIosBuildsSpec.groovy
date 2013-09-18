package com.apphance.flow.ios

import com.apphance.flow.configuration.ios.IOSFamily
import org.gradle.tooling.ProjectConnection
import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.tooling.GradleConnector.newConnector

class ExecuteIosBuildsSpec extends Specification {

    @Shared List<String> GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled',
            '-XX:+CMSPermGenSweepingEnabled', '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    @Shared File projectScheme = new File('demo/ios/GradleXCode')
    @Shared File projectWorkspace = new File('demo/ios/GradleXCodeWS')
    @Shared ProjectConnection projectSchemeConnection
    @Shared ProjectConnection projectWorkspaceConnection

    def setupSpec() {
        projectSchemeConnection = newConnector().forProjectDirectory(projectScheme).connect()
        projectWorkspaceConnection = newConnector().forProjectDirectory(projectWorkspace).connect()
    }

    def cleanupSpec() {
        projectSchemeConnection.close()
        projectWorkspaceConnection.close()
    }

    def 'single device variant is archived'() {
        when:
        runGradle(projectSchemeConnection, 'cleanFlow', 'archiveGradleXCode')

        then:
        noExceptionThrown()

        then:
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCode/'
        new File(projectScheme, "$path/GradleXCode-1.0_32.ipa").exists()
        new File(projectScheme, "$path/GradleXCode-1.0_32.mobileprovision").exists()
        new File(projectScheme, "$path/GradleXCode-1.0_32.zip").exists()
        new File(projectScheme, "$path/GradleXCode-1.0_32_xcarchive.zip").exists()
        new File(projectScheme, "$path/GradleXCode-1.0_32_dSYM.zip").exists()
        new File(projectScheme, "$path/manifest.plist").exists()
        new File(projectScheme, "$path/GradleXCode-1.0_32_ahSYM").exists()
        new File(projectScheme, "$path/GradleXCode-1.0_32_ahSYM").listFiles().size() > 0
    }

    def 'single device scheme variant with apphance is archived'() {
        when:
        runGradle(projectSchemeConnection, 'cleanFlow', 'archiveGradleXCodeWithApphance')

        then:
        noExceptionThrown()

        then:
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCodeWithApphance/'
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32.ipa").exists()
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32.mobileprovision").exists()
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32.zip").exists()
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32_xcarchive.zip").exists()
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32_dSYM.zip").exists()
        new File(projectScheme, "$path/manifest.plist").exists()
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32_ahSYM").exists()
        new File(projectScheme, "$path/GradleXCodeWithApphance-1.0_32_ahSYM").listFiles().size() > 0
        new File(projectScheme, "flow-tmp/GradleXCodeWithApphance/Apphance-Pre-Production.framework").exists()
    }

    def 'single device workspace variant with apphance is archived'() {
        when:
        runGradle(projectWorkspaceConnection, 'cleanFlow', 'archiveGradleXCodeWSGradleXCodeWithApphance')

        then:
        noExceptionThrown()

        then:
        def name = 'GradleXCodeWSGradleXCodeWithApphance'
        def path = "flow-ota/GradleXCodeWS/1.0_32/$name"
        new File(projectWorkspace, "$path/$name-1.0_32.ipa").exists()
        new File(projectWorkspace, "$path/$name-1.0_32.mobileprovision").exists()
        new File(projectWorkspace, "$path/$name-1.0_32.zip").exists()
        new File(projectWorkspace, "$path/$name-1.0_32_xcarchive.zip").exists()
        new File(projectWorkspace, "$path/$name-1.0_32_dSYM.zip").exists()
        new File(projectWorkspace, "$path/manifest.plist").exists()
        new File(projectWorkspace, "$path/$name-1.0_32_ahSYM").exists()
        new File(projectWorkspace, "$path/$name-1.0_32_ahSYM").listFiles().size() > 0
        new File(projectWorkspace, "flow-tmp/$name/Apphance-Pre-Production.framework").exists()
    }

    def 'exception is raised on test fail during build'() {
        when:
        runGradle(projectSchemeConnection, 'cleanFlow', 'testGradleXCodeWithSpace')

        then:
        def e = thrown(Exception)
        def msg = e.cause.cause.cause.message
        msg.startsWith("Error while executing tests for variant: GradleXCode With Space, target: GradleXCodeFailingTests, configuration Debug. For further details investigate test results:")
        msg.endsWith("test-GradleXCode With Space-GradleXCodeFailingTests.xml")
    }

    def 'single simulator variant is archived'() {
        when:
        runGradle(projectSchemeConnection, 'cleanFlow', 'archiveGradleXCodeSimulator')

        then:
        noExceptionThrown()

        then:
        IOSFamily.values().every {
            def f = new File(projectScheme,
                    "flow-ota/GradleXCode/1.0_32/GradleXCodeSimulator/GradleXCodeSimulator-1.0_32-${it.iFormat()}-sim-img.dmg")
            f.exists() && f.size() > 30000
        }
    }

    def runGradle(ProjectConnection pc, String... tasks) {
        def buildLauncher = pc.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).run()
    }
}
