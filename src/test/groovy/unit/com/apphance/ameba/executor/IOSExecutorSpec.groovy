package com.apphance.ameba.executor

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.google.common.io.Files
import spock.lang.Specification

import static com.apphance.ameba.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

class IOSExecutorSpec extends Specification {

    def fileLinker = Mock(FileLinker)
    def logFileGenerator = Mock(CommandLogFilesGenerator)

    def executor = new CommandExecutor(fileLinker, logFileGenerator)

    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]

    def setup() {
        fileLinker.fileLink(_) >> ''
        logFileGenerator.commandLogFiles() >> logFiles
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
    }

    def 'pbxproj is converted to xml format well'() {
        given:
        def tmpDir = Files.createTempDir()

        and:
        Files.copy(
                new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/$PROJECT_PBXPROJ"),
                new File(tmpDir, PROJECT_PBXPROJ))

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.xcodeDir >> new FileProperty(value: tmpDir)
        conf.rootDir >> tmpDir

        and:
        def iosExecutor = new IOSExecutor()
        iosExecutor.commandExecutor = executor
        iosExecutor.conf = conf

        when:
        def xml = iosExecutor.pbxProjToXml()
        xml = xml.join('\n')

        then:
        noExceptionThrown()

        and:
        xml.startsWith('<?xml version="1.0" encoding="UTF-8"?>')

        cleanup:
        tmpDir.deleteDir()
    }
}
