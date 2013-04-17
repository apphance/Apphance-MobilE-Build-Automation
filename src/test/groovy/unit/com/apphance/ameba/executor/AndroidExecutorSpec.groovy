package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.CommandExecutor
import spock.lang.Specification

class AndroidExecutorSpec extends Specification {

    CommandExecutor commandExecutor = Mock()
    File file = Mock()
    AndroidExecutor androidExecutor = new AndroidExecutor(commandExecutor)

    void "test updateProject method"() {
        when: androidExecutor.updateProject(file, 'android-8', 'sample-name')
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == 'android update project -p . -t android-8 -n sample-name -s' })
    }

    void "test listAvd"() {
        when: androidExecutor.listAvd(file)
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == 'android list avd -c' })
    }

    void "test run method"() {
        when: androidExecutor.run(file, "command")
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == 'android command' })

    }
}
