package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.google.inject.Inject
import org.gradle.api.GradleException

/**
 * Executor of ant targets.
 */
class AntExecutor {

    public static String DEBUG = "debug"
    public static String CLEAN = "clean"
    public static String INSTRUMENT = "instrument"

    @Inject
    CommandExecutor executor

    File rootDir

    def executeTarget(File rootDir, String command, Map params = [:]) {
        try {
            executor.executeCommand(new Command([runDir: rootDir, cmd: "ant $command".split(), failOnError: true] + params))
        } catch (IOException e) {
            throw new GradleException("Error during execution: ant $command, $params", e)
        }
    }
}
