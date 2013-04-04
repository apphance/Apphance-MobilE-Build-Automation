package com.apphance.ameba.configuration.android

import org.gradle.api.Project
import spock.lang.Specification

class AndroidConfigurationSpec extends Specification {

    def 'linkedLibraryJars and libraryJars are filled correctly'() {
        given:
        def project = Mock(Project)
        project.rootDir >> new File('testProjects/android/android-basic')

        and:
        def androidConf = new AndroidConfiguration(project, * [null] * 4)

        expect:
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.jarLibraries*.name
        ['subproject', 'subsubproject'] == androidConf.linkedJarLibraries*.parentFile.parentFile.name
    }

    def 'sdk jars are filled correctly'() {
        given:
        def project = Mock(Project)
        project.rootDir >> new File('testProjects/android/android-basic')

        and:
        def androidConf = new AndroidConfiguration(project, * [null] * 4)
        androidConf.minTarget.value = 'android-8'
        androidConf.sdkDir.value = new File(System.getenv('ANDROID_HOME'))

        expect:
        ['FlurryAgent.jar', 'development-apphance.jar'] == androidConf.sdkJars*.name
    }

}
