package com.apphance.flow.di

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.name.Names
import org.gradle.api.Project
import spock.lang.Specification

import javax.inject.Inject

import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANDROID

class ConfigurationModuleSpec extends Specification {

    @Inject AndroidConfiguration androidConf1
    @Inject AndroidConfiguration androidConf2

    @Inject ReleaseConfiguration releaseConfiguration
    @Inject ProjectConfiguration projectConfiguration

    @Inject
    Map<Integer, AbstractConfiguration> configurations

    Injector injector

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
                bindConstant().annotatedWith(Names.named('executable.android')).to(STD_EXECUTABLE_ANDROID)
            }

        }

        injector = Guice.createInjector(module, new ConfigurationModule(project))
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

    def 'variants created'() {
        expect:
        injector.getInstance(AndroidVariantsConfiguration).variantsNames.value.sort() == ['Debug', "Release"]
    }
}
