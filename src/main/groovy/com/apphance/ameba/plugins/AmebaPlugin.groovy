package com.apphance.ameba.plugins

import com.apphance.ameba.di.CommandExecutorModule
import com.apphance.ameba.di.ConfigurationModule
import com.apphance.ameba.di.EnvironmentModule
import com.apphance.ameba.di.GradleModule
import com.google.inject.Guice
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

class AmebaPlugin implements Plugin<Project> {

    def l = getLogger(getClass())

    @Override
    void apply(Project project) {
        def injector = Guice.createInjector(
                new GradleModule(project),
                new ConfigurationModule(project),
                new EnvironmentModule(),
                new CommandExecutorModule(project),
        )
        injector.getInstance(PluginMaster).enhanceProject(project)

        project.tasks.each { injector.injectMembers(it) }
    }
}
