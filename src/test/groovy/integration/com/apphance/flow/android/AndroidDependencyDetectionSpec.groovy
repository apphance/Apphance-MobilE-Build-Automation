package com.apphance.flow.android

import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.FlowPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

@Ignore('requires compiled project to run')
class AndroidDependencyDetectionSpec extends Specification {

    def fileLinker = Mock(FileLinker)
    def logFileGenerator = Mock(CommandLogFilesGenerator)

    def executor = new CommandExecutor(fileLinker, logFileGenerator)

    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]

    def setup() {
        fileLinker.fileLink(_) >> ''
        logFileGenerator.commandLogFiles() >> logFiles
    }

    def 'project dependencies are configured correctly'() {
        given:
        def projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File('testProjects/android/android-basic'))

        and:
        def project = projectBuilder.build()
        project.project.plugins.apply(FlowPlugin)

        when:
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['ant', 'debug']))
        def androidConf

        then:
        androidConf.sdkDirectory
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.libraryJars*.name
        ['subproject', 'subsubproject'] == androidConf.linkedLibraryJars*.parentFile.parentFile.name
    }
}
