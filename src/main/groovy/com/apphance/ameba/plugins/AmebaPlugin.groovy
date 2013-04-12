package com.apphance.ameba.plugins

import com.apphance.ameba.di.CommandExecutorModule
import com.apphance.ameba.di.ConfigurationModule
import com.apphance.ameba.di.EnvironmentModule
import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

class AmebaPlugin implements Plugin<Project> {

    def l = getLogger(getClass())

    @Override
    void apply(Project project) {
        l.lifecycle(AMEBA_ASCII_ART)

        def injector = Guice.createInjector(
                new ConfigurationModule(project),
                new EnvironmentModule(),
                new CommandExecutorModule(project),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Project).toInstance(project)
                    }
                })
        injector.getInstance(PluginMaster).enhanceProject(project)

        project.tasks.each { injector.injectMembers(it) }
    }

    static String AMEBA_ASCII_ART = '''\

                          _
                         ( )
   _ _   ___ ___     __  | |_      _ _
 /'_` )/' _ ` _ `\\ /'__`\\| '_`\\  /'_` )
( (_| || ( ) ( ) |(  ___/| |_) )( (_| |
`\\__,_)(_) (_) (_)`\\____)(_,__/'`\\__,_)

'''

}
