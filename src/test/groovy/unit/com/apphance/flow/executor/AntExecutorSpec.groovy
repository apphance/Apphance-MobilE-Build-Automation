package com.apphance.flow.executor

import com.apphance.flow.TestUtils
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.util.FlowUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANT
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.nio.file.Files.deleteIfExists

@Mixin([FlowUtils, TestUtils])
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

    @Unroll
    def 'test signing parameters. System properties: #systemProps'() {
        given:
        antExecutor.executor = GroovyMock(CommandExecutor)
        systemProps.each { key, val -> System.setProperty(key, val) }

        when:
        antExecutor.executeTarget temporaryDir, CLEAN

        then:
        1 * antExecutor.executor.executeCommand({ Command com ->
            assert com.commandForExecution == ['ant', 'clean'] + expectedAdditionalParams
            assert com.cmd == ['ant', 'clean'] + cmd
            true
        })

        cleanup:
        systemProps.each { key, val -> System.clearProperty(key) }

        where:
        systemProps                                 | expectedAdditionalParams                  | cmd
        [:]                                         | []                                        | []
        ['key.alias': 'al']                         | ['-Dkey.alias=al']                        | ['-Dkey.alias=$keyAlias']
        ['key.alias': 'al', 'key.alias.pass': 'ps'] | ['-Dkey.alias=al', '-Dkey.alias.pass=ps'] | ['-Dkey.alias=$keyAlias', '-Dkey.alias.pass=$keyAliasPass']
    }
}
