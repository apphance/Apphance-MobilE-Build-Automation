package com.apphance.ameba.executor

import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.log.CommandLogFileGenerator
import spock.lang.Specification

class CommandExecutorSpec extends Specification {

    //TODO mocki prywatnych metod ???
    //TODO przetestować input
    //TODO przetestować environment variables

    def "excutor invokes 'ls' command"() {

        given:
        def executor = new CommandExecutor()
        executor.fileLinker = Mock(FileLinker)
        executor.logFileGenerator = Mock(CommandLogFileGenerator)

        and:
        executor.fileLinker.fileLink(_) >> ''
        executor.logFileGenerator.commandLogFile() >> File.createTempFile('tmp', 'file')

        expect:
        expectedOutput == executor.executeCommand(new Command(cmd: cmd, runDir: runDir))

        where:
        expectedOutput          | runDir                  | cmd
        ['groovy', 'resources'] | new File('src', 'test') | ['ls']
    }

    def 'executor fails with invalid command'() {

        given:
        def executor = new CommandExecutor()

        and:
        executor.fileLinker = Mock(FileLinker)
        executor.logFileGenerator = Mock(CommandLogFileGenerator)
        executor.fileLinker.fileLink(_) >> ''
        executor.logFileGenerator.commandLogFile() >> File.createTempFile('tmp', 'file')

        and:
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
        def executor = new CommandExecutor()

        and:
        executor.fileLinker = Mock(FileLinker)
        executor.logFileGenerator = Mock(CommandLogFileGenerator)
        executor.fileLinker.fileLink(_) >> ''
        executor.logFileGenerator.commandLogFile() >> File.createTempFile('tmp', 'file')

        and:
        def command = new Command(cmd: ['ASDAFSFAG'], runDir: new File('src', 'test'), failOnError: false)

        when:
        def output = executor.executeCommand(command)

        then:
        output == []
    }

    def "executor invokes 'ls' command with dir passed through env variable"() {
        given:
        def executor = new CommandExecutor()
        executor.fileLinker = Mock(FileLinker)
        executor.logFileGenerator = Mock(CommandLogFileGenerator)

        and:
        executor.fileLinker.fileLink(_) >> ''
        executor.logFileGenerator.commandLogFile() >> File.createTempFile('tmp', 'file')

        expect:
        def command = new Command(cmd: cmd, runDir: runDir, environment: env, failOnError: false)
        println command
        expectedOutput == executor.executeCommand(command)

        where:
        expectedOutput          | runDir                  | cmd                  | env
        ['groovy', 'resources'] | new File('src', 'test') | ['ls', '$DIR_TO_LS'] | [DIR_TO_LS: new File('src', 'test').parentFile.canonicalPath]
    }
}

