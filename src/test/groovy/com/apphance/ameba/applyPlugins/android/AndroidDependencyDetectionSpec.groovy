package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.executor.Command
import com.apphance.ameba.executor.CommandExecutor
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.log.CommandLogFilesGenerator
import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.STD
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
        def project = projectBuilder.build()
        project.project.plugins.apply(AmebaPlugin.class)

        when:
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['ant', 'debug']))
        def androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)

        then:
        androidConf.sdkDirectory
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.libraryJars.collect { it.name }
        ['subproject', 'subsubproject'] == androidConf.linkedLibraryJars.collect { it.parentFile.parentFile.name }

    }
}
