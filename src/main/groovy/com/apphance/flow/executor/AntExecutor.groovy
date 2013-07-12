package com.apphance.flow.executor

import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import org.gradle.api.GradleException

import javax.inject.Inject

/**
 * Executor of ant targets.
 */
class AntExecutor {

    public static String DEBUG = "debug"
    public static String CLEAN = "clean"
    public static String INSTRUMENT = "instrument"

    @Inject
    CommandExecutor executor

    void executeTarget(File rootDir, String command, Map params = [:]) {
        try {
            executor.executeCommand(new Command([runDir: rootDir, cmd: "ant $command".split(), failOnError: true] + params))
        } catch (IOException e) {
            throw new GradleException("Error during execution: ant $command, $params", e)
        }
    }
}
