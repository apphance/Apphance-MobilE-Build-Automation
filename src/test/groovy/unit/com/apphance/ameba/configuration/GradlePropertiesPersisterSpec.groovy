package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.di.ConfigurationModule
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification

class GradlePropertiesPersisterSpec extends Specification {

    @Shared File rootDir = new File('src/test/resources/com/apphance/ameba/configuration/persister')

    private Injector injector

    private Project project

    private AbstractModule module

    def setup() {
        project = Mock()
        module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Project).toInstance(project)
            }
        }
    }

    def 'correct root dir'() {
        expect: rootDir.exists()
    }

    def "test persister get"() {
        given:
        project.getRootDir() >> this.rootDir
        injector = Guice.createInjector(module, new ConfigurationModule())
        def persister = injector.getInstance(PropertyPersister)

        expect:
        persister.get('apphance.appkey') == 'exampleAppKey'
    }

    def "test persister save"() {
        given:
        def tempDir = Files.createTempDir()
        project.getRootDir() >> tempDir
        injector = Guice.createInjector(module, new ConfigurationModule())
        def persister = injector.getInstance(PropertyPersister)

        def androidConfiguration = new AndroidConfiguration()
        androidConfiguration.logDir.value = tempDir
        androidConfiguration.versionString.value = 'version string'

        def projectConfiguration = new IOSConfiguration()
        projectConfiguration.name.value = 'Project name'

        when:
        persister.save([androidConfiguration, projectConfiguration])

        then:
        persister.get(androidConfiguration.logDir.name) == tempDir.absolutePath
        persister.get(androidConfiguration.versionString.name) == 'version string'

        persister.get(projectConfiguration.name.name) == 'Project name'

        persister.get('nonexisting') == null

        cleanup:
        tempDir.delete()
    }

    def "backup made"() {
        given:
        def tempDir = Files.createTempDir()
        project.getRootDir() >> tempDir
        injector = Guice.createInjector(module, new ConfigurationModule())

        def persister = injector.getInstance(PropertyPersister)

        expect:
        countFiles(tempDir) == 0

        when:
        persister.save([])
        persister.save([])
        sleep(1)
        persister.save([])

        then:
        countFiles(tempDir) == 3

        cleanup:
        tempDir.delete()
    }


    def 'test timestamp'() {
        expect:
        GradlePropertiesPersister.timeStamp ==~ /20\d{15}/
    }

    def countFiles(File directory) {
        def number = 0
        directory.eachFile { number++ }
        number
    }
}
