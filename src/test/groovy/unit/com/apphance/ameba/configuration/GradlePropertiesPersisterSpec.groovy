package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.detection.ProjectType
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.di.ConfigurationModule
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class GradlePropertiesPersisterSpec extends Specification {

    @Shared File rootDir = new File('src/test/resources/com/apphance/ameba/configuration/persister')

    private Injector injector

    private Project project

    private AbstractModule module

    def setup() {
        project = Mock()

        def logFileGenerator = Mock(CommandLogFilesGenerator)
        def fileLinker = Mock(FileLinker)

        module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(FileLinker).toInstance(fileLinker)
                bind(CommandLogFilesGenerator).toInstance(logFileGenerator)

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
        injector = Guice.createInjector(module, new ConfigurationModule(project))
        def persister = injector.getInstance(PropertyPersister)

        expect:
        persister.get('apphance.appkey') == 'exampleAppKey'
    }

    def "test persister save"() {
        given:
        def tempDir = Files.createTempDir()
        project.getRootDir() >> tempDir
        injector = Guice.createInjector(module, fakeConfModule())
        def persister = injector.getInstance(PropertyPersister)

        def androidConfiguration = new AndroidConfiguration(project, * [null] * 3, Mock(ProjectTypeDetector) {
            detectProjectType(_) >> ANDROID
        })
        androidConfiguration.logDir.value = tempDir
        androidConfiguration.versionString.value = 'version string'

        def iOSConfiguration = new IOSConfiguration()
        iOSConfiguration.projectTypeDetector = Mock(ProjectTypeDetector) {
            detectProjectType(_) >> IOS
        }
        iOSConfiguration.project = project
        iOSConfiguration.name.value = 'Project name'

        when:
        persister.save([androidConfiguration, iOSConfiguration])

        then:
        persister.get(androidConfiguration.logDir.name) == tempDir.absolutePath
        persister.get(androidConfiguration.versionString.name) == 'version string'
        persister.get(iOSConfiguration.name.name) == 'Project name'
        persister.get('nonexisting') == null

        cleanup:
        tempDir.delete()
    }

    def "backup made"() {
        given:
        def tempDir = Files.createTempDir()
        project.getRootDir() >> tempDir
        injector = Guice.createInjector(module, fakeConfModule())

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

    def fakeConfModule() {
        def confModule = new ConfigurationModule(project)
        def typeDetector = Mock(ProjectTypeDetector)
        confModule.typeDetector = typeDetector
        typeDetector.detectProjectType(_) >> ANDROID
        confModule
    }
}
