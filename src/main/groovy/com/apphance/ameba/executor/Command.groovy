package com.apphance.ameba.executor

import org.gradle.api.Project

class Command {

    Project project

    File runDir//TODO return project.rootDir if empty
    Collection<String> cmd
    Collection<String> envp = null
    Collection<String> input

    File output

    String escapePrefix = '##'

    boolean failOnError = true
    boolean silent = false

    @Override
    public String toString() {
        this.properties
    }
}
