package com.apphance.ameba.android

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

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
        project.project.plugins.apply(AmebaPlugin)

        when:
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['ant', 'debug']))
        def androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)

        then:
        androidConf.sdkDirectory
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.libraryJars*.name
        ['subproject', 'subsubproject'] == androidConf.linkedLibraryJars*.parentFile.parentFile.name
    }
}
