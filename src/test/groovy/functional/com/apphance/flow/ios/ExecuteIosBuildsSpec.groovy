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
    @Shared File projectFramework = new File('demo/ios/GRadleXCodeFW')
    @Shared ProjectConnection projectSchemeConn
    @Shared ProjectConnection projectWorkspaceConn
    @Shared ProjectConnection projectFrameworkConn

    def setupSpec() {
        projectSchemeConn = newConnector().forProjectDirectory(projectScheme).connect()
        projectWorkspaceConn = newConnector().forProjectDirectory(projectWorkspace).connect()
        projectFrameworkConn = newConnector().forProjectDirectory(projectFramework).connect()
    }

    def cleanupSpec() {
        projectSchemeConn.close()
        projectWorkspaceConn.close()
        projectFrameworkConn.close()
    }

    def 'single device variant is archived'() {
        when:
        runGradle(projectSchemeConn, 'cleanFlow', 'archiveGradleXCode')

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
        def report = new File(projectScheme, 'flow-tmp/GradleXCode/test-GradleXCode.xml')
        report.exists()
        report.size() > 0
    }

    def 'single device scheme variant with apphance is archived'() {
        when:
        runGradle(projectSchemeConn, 'cleanFlow', 'archiveGradleXCodeWithApphance')

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
        runGradle(projectWorkspaceConn, 'cleanFlow', 'archiveGradleXCodeWSGradleXCodeWithApphance')

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
        runGradle(projectSchemeConn, 'cleanFlow', 'testGradleXCodeWithSpace')

        then:
        def e = thrown(Exception)
        def msg = e.cause.cause.cause.message
        msg.startsWith("Error while executing tests for variant: GradleXCode With Space, scheme: GradleXCode With Space. For further details investigate test results:")
        msg.endsWith("test-GradleXCode With Space.xml")
        def report = new File(projectScheme, 'flow-tmp/GradleXCode With Space/test-GradleXCode With Space.xml')
        report.exists()
        report.size() > 0
    }

    def 'single simulator variant is archived'() {
        when:
        runGradle(projectSchemeConn, 'cleanFlow', 'archiveGradleXCodeSimulator')

        then:
        noExceptionThrown()

        then:
        IOSFamily.values().every {
            def f = new File(projectScheme,
                    "flow-ota/GradleXCode/1.0_32/GradleXCodeSimulator/GradleXCodeSimulator-1.0_32-${it.iFormat()}-sim-img.dmg")
            f.exists() && f.size() > 30000
        }
    }

    def 'single framework variant is built'() {
        given:
        System.properties['version.string'] = '1.0'
        System.properties['version.code'] = '32'

        when:
        runGradle(projectFrameworkConn, 'cleanFlow', 'frameworkGradleXCodeFW')

        then:
        noExceptionThrown()

        then:
        def path = 'flow-ota/GradleXCode/1.0_32/GradleXCodeFW/'
        new File(projectFramework, "$path/GradleXCodeFW-1.0_32-GradleXCodeFramework.zip").exists()

        cleanup:
        System.properties.remove('version.string')
        System.properties.remove('version.code')
    }

    def runGradle(ProjectConnection pc, String... tasks) {
        def buildLauncher = pc.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS as String[])
        buildLauncher.forTasks(tasks).withArguments("-PflowProjectPath=${new File('.').absolutePath}", '-i').run()
    }
}
