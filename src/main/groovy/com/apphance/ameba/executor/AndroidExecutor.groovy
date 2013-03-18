package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.google.inject.Inject

class AndroidExecutor {

    CommandExecutor executor

    @Inject
    AndroidExecutor(CommandExecutor executor) {
        this.executor = executor
    }

    def updateProject(File directory) {
        run(directory, "update project -p . -s")
    }

    def listAvd(File directory) {
        run(directory, "list avd -c")
    }

    def run(File directory, String command) {
        executor.executeCommand(new Command(runDir: directory, cmd: "android $command".split(), failOnError: false))
    }
}