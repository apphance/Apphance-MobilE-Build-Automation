package com.apphance.ameba.di

import com.apphance.ameba.configuration.AndroidConfiguration
import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.detection.ProjectTypeDetector
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import org.gradle.api.Project
import spock.lang.Specification

class ConfigurationModuleSpec extends Specification {

    @Inject AndroidConfiguration androidConf1
    @Inject AndroidConfiguration androidConf2

    @Inject ProjectConfiguration projectConf1
    @Inject ProjectConfiguration projectConf2

    @Inject
    Set<Configuration> configurations


    def setup() {
        def project = Mock(Project)
        def projectTypeDetector = Mock(ProjectTypeDetector)

        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Project) toInstance(project)
                bind(ProjectTypeDetector).toInstance(projectTypeDetector)
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

        projectConf1 != null
        projectConf2 != null
        projectConf1.is(projectConf2)
    }

    def 'test multibinder'() {
        expect:
        configurations.size() == 2
        [projectConf1, androidConf1].sort() == configurations.sort()
    }
}
