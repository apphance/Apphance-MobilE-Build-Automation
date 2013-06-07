package com.apphance.flow.executor.command

class CommandFailedException extends RuntimeException {

    private Command c

    CommandFailedException(String message, Command c) {
        super(message)
        this.c = c
    }

    Command getCommand() {
        return c
    }
}
