package com.apphance.ameba.configuration

import com.apphance.ameba.di.ConfigurationModule
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID

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
        androidConfiguration.sdkDir.value = tempDir
        androidConfiguration.minSdkTargetName.value = 'min target name'

        def projectConfiguration = new ProjectConfiguration()
        projectConfiguration.name.value = 'Project name'
        projectConfiguration.type.value = ANDROID

        when:
        persister.save([androidConfiguration, projectConfiguration])

        then:
        persister.get(androidConfiguration.sdkDir.name) == tempDir.absolutePath
        persister.get(androidConfiguration.minSdkTargetName.name) == 'min target name'

        persister.get(projectConfiguration.name.name) == 'Project name'
        persister.get(projectConfiguration.type.name) == ANDROID.toString()

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
