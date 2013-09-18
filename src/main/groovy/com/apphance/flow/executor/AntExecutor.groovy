package com.apphance.flow.executor

import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.util.FlowUtils

import javax.inject.Inject
import javax.inject.Named

@Mixin(FlowUtils)
class AntExecutor {

    public static String DEBUG = 'debug'
    public static String CLEAN = 'clean'

    @Inject
    CommandExecutor executor
    @Inject
    @Named('executable.ant') ExecutableCommand executableAnt

    void executeTarget(File rootDir, String command) {
        def signProps = System.getProperties().findAll { it.key.startsWith('key.') }
        def commandProps = signProps.keySet().collect { "-D$it=\$${dotToCamel(it)}" }
        def secretParams = signProps.collectEntries { String key, String value -> [(dotToCamel(key)): value] }

        executor.executeCommand new Command(runDir: rootDir, cmd: executableAnt.cmd + [command] + commandProps, failOnError: true, secretParams: secretParams)
    }
}

