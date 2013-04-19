package com.apphance.ameba.configuration.android

import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class AndroidConfigurationSpec extends Specification {

    def 'android analysis configuration is enabled based on project type'() {
        given:
        def p = Mock(Project)

        and:
        def ptd = Mock(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration(p, * [null] * 3, ptd, null)

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
        def project = Mock(Project)
        project.rootDir >> new File('testProjects/android/android-basic')

        and:
        def androidConf = new AndroidConfiguration(project, * [null] * 5)

        expect:
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.jarLibraries*.name
        ['subproject', 'subsubproject'] == androidConf.linkedJarLibraries*.parentFile.parentFile.name
    }

    def 'no exception during readProperties'() {
        given:
        def project = ProjectBuilder.builder().build()
        def androidConfiguration = new AndroidConfiguration(project, * [null] * 5)

        expect:
        androidConfiguration.readProperties()
    }
}
