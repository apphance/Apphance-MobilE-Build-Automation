package com.apphance.flow.executor

import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.util.FlowUtils
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANT
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.nio.file.Files.deleteIfExists

@Mixin(FlowUtils)
class AntExecutorSpec extends Specification {

    @Shared File rootDir = new File('src/test/resources/com/apphance/flow/executor/antTestProject')

    def antExecutor = new AntExecutor(executableAnt: STD_EXECUTABLE_ANT)

    def setup() {
        def fileLinker = Stub(FileLinker)
        def logFilesGenerator = Stub(CommandLogFilesGenerator)
        def executor = new CommandExecutor(fileLinker, logFilesGenerator)
        logFilesGenerator.commandLogFiles() >> [(STD): tempFile, (ERR): tempFile]

        antExecutor.executor = executor
    }

    def "successfully execute target"() {
        given:
        def md5File = new File(rootDir.absolutePath + '/' + 'build.xml.MD5')
        deleteIfExists(md5File.toPath())

        expect: !md5File.exists()
        when: antExecutor.executeTarget rootDir, "testTarget"
        then: md5File.exists()
        cleanup: deleteIfExists(md5File.toPath())
    }
}
