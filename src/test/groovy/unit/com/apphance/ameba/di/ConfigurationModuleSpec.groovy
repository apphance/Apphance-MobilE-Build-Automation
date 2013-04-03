package com.apphance.ameba.di

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import org.gradle.api.Project
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir

class ConfigurationModuleSpec extends Specification {

    @Inject AndroidConfiguration androidConf1
    @Inject AndroidConfiguration androidConf2

    @Inject IOSConfiguration iosConf1
    @Inject IOSConfiguration iosConf2

    @Inject
    Set<Configuration> configurations


    def setup() {
        def fileLinker = Mock(FileLinker)
        def logFileGenerator = Mock(CommandLogFilesGenerator)

        def rootDir = createTempDir()
        rootDir.deleteOnExit()

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

        Guice.createInjector(module, new ConfigurationModule())
    }

    def "configurations are singletons"() {
        expect:
        androidConf1 != null
        androidConf2 != null
        androidConf1.is(androidConf2)

        iosConf1 != null
        iosConf2 != null
        iosConf1.is(iosConf2)
    }

    def 'test multibinder'() {
        expect:
        configurations.size() == 2
        [iosConf1, androidConf1].sort() == configurations.sort()
    }
}
