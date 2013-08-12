package com.apphance.flow.executor.command

import org.gradle.api.GradleException

class CommandFailedException extends GradleException {

    final Command command
    final File stdoutLog
    final File stderrLog

    CommandFailedException(String message, Command c, File stdoutLog = null, File stderrLog = null) {
        super(message)
        this.command = c
        this.stdoutLog = stdoutLog
        this.stderrLog = stderrLog
    }
}
