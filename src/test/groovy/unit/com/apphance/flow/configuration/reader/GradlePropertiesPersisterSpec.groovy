package com.apphance.flow.configuration.reader

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.di.ConfigurationModule
import com.apphance.flow.executor.ExecutableCommand
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.google.common.io.Files
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.name.Names
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS
import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANDROID

class GradlePropertiesPersisterSpec extends Specification {

    @Shared File rootDir = new File('src/test/resources/com/apphance/flow/configuration/persister')

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
                bindConstant().annotatedWith(Names.named('executable.android')).to(STD_EXECUTABLE_ANDROID)
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

        def androidConfiguration = new AndroidConfiguration()
        androidConfiguration.projectTypeDetector = GroovyStub(ProjectTypeDetector) {
            detectProjectType(_) >> ANDROID
        }
        androidConfiguration.project = project
        androidConfiguration.target.value = 'test target'

        def iOSConfiguration = new IOSConfiguration()
        iOSConfiguration.project = project
        iOSConfiguration.projectTypeDetector = Mock(ProjectTypeDetector) { detectProjectType(_) >> IOS }
        iOSConfiguration.sdk.value = 'iphoneos'
        iOSConfiguration.simulatorSdk.value = 'iphoneossimulator'

        when:
        persister.save([androidConfiguration, iOSConfiguration])
        persister.init(project)

        then:
        persister.get(androidConfiguration.target.name) == 'test target'
        persister.get(iOSConfiguration.sdk.name) == 'iphoneos'
        persister.get(iOSConfiguration.simulatorSdk.name) == 'iphoneossimulator'
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
