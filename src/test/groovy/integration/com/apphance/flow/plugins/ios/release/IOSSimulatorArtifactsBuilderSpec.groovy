package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile

class IOSSimulatorArtifactsBuilderSpec extends Specification {

    def executor = new CommandExecutor(
            Mock(FileLinker) {
                fileLink(_) >> ''
            },
            Mock(CommandLogFilesGenerator) {
                commandLogFiles() >> [(STD): createTempFile('tmp', 'out'), (ERR): createTempFile('tmp', 'err')]
            }
    )
    def builder = new IOSSimulatorArtifactsBuilder(
            conf: GroovyMock(IOSConfiguration) {
                getRootDir() >> new File('.')
                getVersionCode() >> '3.1.45'
                getVersionString() >> '101'
            },
            executor: executor
    )

    def 'ios_sim_template is synced'() {
        given:
        def tmplDir = new File(getClass().getResource('ios_sim_tmpl').toURI())
        def tmpDir = createTempDir()

        when:
        builder.syncSimAppTemplateToTmpDir(tmplDir, tmpDir)

        then:
        def contents = new File(tmpDir, 'Contents')
        contents.exists()
        contents.isDirectory()
        folderSize(contents) < 410000

        and:
        def launcher = new File(contents, 'MacOS/Launcher')
        launcher.exists()
        launcher.isFile()
        launcher.canExecute()

        cleanup:
        tmpDir.deleteDir()
    }

    long folderSize(File directory) {
        long length = 0
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length()
            else
                length += folderSize(file)
        }
        length
    }

    def 'tmpDir is created with appDir inside'() {
        when:
        def tmpDir = builder.tmpDir(GroovyMock(IOSBuilderInfo) {
            getProductName() >> 'Some Application'
        }, family)

        then:
        tmpDir.isDirectory()
        tmpDir.name == "Some Application (${family.iFormat()}_Simulator) 101_3.1.45.app"

        where:
        family << IOSFamily.values()
    }
}
