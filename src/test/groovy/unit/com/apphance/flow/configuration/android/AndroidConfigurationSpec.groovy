package com.apphance.flow.configuration.android

import com.apphance.flow.detection.project.ProjectTypeDetector
import org.gradle.api.Project
import spock.lang.Ignore
import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidConfigurationSpec extends Specification {

    def 'android analysis configuration is enabled based on project type'() {
        given:
        def p = GroovyStub(Project) {
            getRootDir() >> GroovyStub(File)
        }

        and:
        def ptd = GroovyStub(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration()
        ac.projectTypeDetector = ptd
        ac.project = p

        then:
        ac.isEnabled() == enabled

        where:
        enabled | type
        false   | IOS
        true    | ANDROID
    }


    @Ignore('FIXME works only on compiled projects')
    def 'linkedLibraryJars and libraryJars are filled correctly'() {
        given:
        def project = GroovyStub(Project)
        project.rootDir >> new File('testProjects/android/android-basic')

        and:
        def androidConf = new AndroidConfiguration(project, * [null] * 5)

        expect:
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.jarLibraries*.name
        ['subproject', 'subsubproject'] == androidConf.linkedJarLibraries*.parentFile.parentFile.name
    }

    def 'no exception during readProperties'() {
        given:
        def androidConfiguration = new AndroidConfiguration()
        androidConfiguration.project = builder().build()

        expect:
        androidConfiguration.readProperties()
    }
}
