package com.apphance.ameba.di

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.google.inject.AbstractModule
import com.google.inject.Guice
import javax.inject.Inject
import org.gradle.api.Project
import spock.lang.Specification

class ConfigurationModuleSpec extends Specification {

    @Inject AndroidConfiguration androidConf1
    @Inject AndroidConfiguration androidConf2

    @Inject ReleaseConfiguration releaseConfiguration
    @Inject ProjectConfiguration projectConfiguration

    @Inject
    Map<Integer, AbstractConfiguration> configurations

    def setup() {
        def fileLinker = Mock(FileLinker)
        def logFileGenerator = Mock(CommandLogFilesGenerator)

        def rootDir = Mock(File)
        rootDir.list() >> ['AndroidManifest.xml']

        def project = Mock(Project)
        project.rootDir >> rootDir

        def projectTypeDetector = Mock(ProjectTypeDetector)

        def module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Project).toInstance(project)
                bind(ProjectTypeDetector).toInstance(projectTypeDetector)

                bind(FileLinker).toInstance(fileLinker)
                bind(CommandLogFilesGenerator).toInstance(logFileGenerator)

                bind(ConfigurationModuleSpec).toInstance(ConfigurationModuleSpec.this)
            }

        }

        Guice.createInjector(module, new ConfigurationModule(project))
    }

    def 'configurations are singletons'() {
        expect:
        androidConf1 != null
        androidConf2 != null
        androidConf1.is(androidConf2)
    }

    def 'multibinder loads configuration in correct order'() {
        expect:
        configurations.hashCode()
        configurations.size() > 0
        configurations.sort().values().toArray()[0].class == AndroidConfiguration
    }

    def 'interfaces bound to correct instance'() {
        expect:
        projectConfiguration.class == AndroidConfiguration
        releaseConfiguration.class == AndroidReleaseConfiguration
    }
}
