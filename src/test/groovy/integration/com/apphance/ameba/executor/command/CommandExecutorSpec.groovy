package com.apphance.ameba.executor.command

import com.apphance.ameba.executor.linker.FileLinker
import spock.lang.Specification

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

class CommandExecutorSpec extends Specification {

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

    def "executor invokes 'ls' command"() {

        given:
        def command = new Command(cmd: cmd, runDir: runDir)

        expect:
        expectedOutput == executor.executeCommand(command)

        where:
        expectedOutput          | runDir                  | cmd
        ['groovy', 'resources'] | new File('src', 'test') | ['ls']
    }

    def 'executor fails with invalid command'() {

        given:
        def command = new Command(cmd: ['ASDAFSFAG'], runDir: new File('src', 'test'))

        when:
        executor.executeCommand(command)

        then:
        def e = thrown(CommandFailedException)
        e.message == 'Cannot run program "ASDAFSFAG" (in directory "src/test"): error=2, No such file or directory'
        e.command == command
    }

    def 'executor not fails on invalid command'() {
        given:
        def command = new Command(cmd: ['ASDAFSFAG'], runDir: new File('src', 'test'), failOnError: false)

        when:
        def output = executor.executeCommand(command)

        then:
        output == []
    }

    def 'executor starts command in background'() {
        given:
        def command = new Command(cmd: ['pwd'], runDir: '.' as File)

        when:
        def process = executor.startCommand(command)
        def exitValue = process.waitFor()

        then:
        process instanceof Process
        exitValue == 0

        then:
        logFiles[STD].exists()
        logFiles[STD].text
        new File(logFiles[STD].text.trim()).exists()
    }

    //this test may by potentially unsafe on windows workstations
    def "executor invokes 'ls' command with dir passed through env variable"() {

        expect:
        def command = new Command(cmd: cmd, runDir: runDir, environment: env, failOnError: false)
        expectedOutput == executor.executeCommand(command)

        where:
        expectedOutput   | runDir                  | cmd                               | env
        ['main', 'test'] | new File('src', 'test') | ['bash', '-c', 'ls \\$DIR_TO_LS'] | [DIR_TO_LS: new File('src', 'test').parentFile.canonicalPath]
    }

    def 'executor handles input correctly'() {
        given:
        def command = new Command(cmd: ['/bin/bash', '-c', 'read V; echo $V'], runDir: '.' as File, input: ['10'], params: [V: '$V'])

        when:
        def output = executor.executeCommand(command)

        then:
        output == ['10']
    }
}
