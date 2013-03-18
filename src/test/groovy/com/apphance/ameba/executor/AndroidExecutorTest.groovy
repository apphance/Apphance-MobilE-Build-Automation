package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.CommandExecutor
import spock.lang.Specification

class AndroidExecutorTest extends Specification{

    CommandExecutor commandExecutor = Mock()
    AndroidExecutor androidExecutor = new AndroidExecutor(commandExecutor)
    File file = Mock()


    void "test updateProject method"() {
        when: androidExecutor.updateProject(file)
        then: 1 * commandExecutor.executeCommand({it.commandForExecution.join(' ') == 'android update project -p . -s'})
    }

    void "test listAvd"() {
        when: androidExecutor.listAvd(file)
        then: 1 * commandExecutor.executeCommand({it.commandForExecution.join(' ') == 'android list avd -c'})
    }

    void "test run method"() {
        when: androidExecutor.run(file, "command")
        then: 1 * commandExecutor.executeCommand({it.commandForExecution.join(' ') == 'android command'})

    }
}
