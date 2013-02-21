package com.apphance.ameba.executor

import org.gradle.api.Project
import spock.lang.Specification

class CommandExecutorSpec extends Specification {

    def executor = new CommandExecutor()

    def 'test executor with `ls` command'() {
        given:
        def project = Mock(Project)

        and:
        project.file(_) >> new File('/tmp')

        expect:
        expectedOutput == executor.executeCommand(new Command(cmd: cmd, runDir: new File(runDir), project: project))

        where:
        expectedOutput          | runDir     | cmd
        ['groovy', 'resources'] | 'src/test' | ['ls']
    }

    def 'test executor with invalid command'() {

        given:
        def project = Mock(Project)

        def command = new Command(cmd: ['ASDAFSFAG'], runDir: new File('src/test'), project: project)

        project.file(_) >> new File('/tmp')

        when:
        executor.executeCommand(command)

        then:
        def e = thrown(CommandFailedException)
        e.message == 'Cannot run program "ASDAFSFAG" (in directory "src/test"): error=2, No such file or directory'
        e.command == command
    }

    def 'test displayable command'() {
        expect:
        displayableCmd == executor.displayableCmd(new Command(cmd: input))

        where:
        displayableCmd                      | input
        "ls"                                | ['ls']
        "ls -al"                            | ['ls', '-al']
        "ls -al 'bolo bolo'"                | ['ls', '-al', '\'bolo bolo\'']
        "ls -al bolo-bolo"                  | ['ls', '-al', 'bolo-bolo']
        "upload arch.jar -u user -p ****"   | ['upload', 'arch.jar', '-u', 'user', '-p', '##pass']
        "upload arch.jar -u user -p pass##" | ['upload', 'arch.jar', '-u', 'user', '-p', 'pass##']
    }

    def 'test escape command'() {
        expect:
        displayableCmd == executor.escapeCommand(new Command(cmd: input))

        where:
        displayableCmd                                       | input
        ['ls']                                               | ['ls']
        ['ls', '-al']                                        | ['ls', '-al']
        ['ls', '-al', '\'bolo bolo\'']                       | ['ls', '-al', '\'bolo bolo\'']
        ['ls', '-al', 'bolo-bolo']                           | ['ls', '-al', 'bolo-bolo']
        ['upload', 'arch.jar', '-u', 'user', '-p', 'pass']   | ['upload', 'arch.jar', '-u', 'user', '-p', '##pass']
        ['upload', 'arch.jar', '-u', 'user', '-p', 'pass##'] | ['upload', 'arch.jar', '-u', 'user', '-p', 'pass##']
    }
}
