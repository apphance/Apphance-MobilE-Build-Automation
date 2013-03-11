package com.apphance.ameba.di

import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.google.inject.AbstractModule
import com.google.inject.Provides
import org.gradle.api.Project

class CommandExecutorModule extends AbstractModule {

    private Project project

    CommandExecutorModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {}

    @Provides
    @javax.inject.Singleton
    CommandLogFilesGenerator commandLogFileGenerator() {
        return new CommandLogFilesGenerator(createLogDir())
    }

    private File createLogDir() {
        def logDir = project.file('log')
        if (!logDir.exists())
            logDir.mkdirs()
        logDir
    }
}
