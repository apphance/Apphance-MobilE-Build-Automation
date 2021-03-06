package com.apphance.flow.detection.project

import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS

class ProjectTypeDetectorSpec extends Specification {

    def detector = new ProjectTypeDetector()

    def 'detects expected project type'() {

        expect:
        detector.detectProjectType(path) == expectedType

        where:
        expectedType | path
        ANDROID      | new File("projects/test/android/android-basic")
        IOS          | new File('demo/ios/GradleXCode')
    }

    def 'should yield on bi-detection'() {
        when:
        detector.detectProjectType(getLocalTestProject('android_ios_project'))

        then:
        def exception = thrown(RuntimeException)
        exception.message =~ 'ANDROID'
        exception.message =~ 'IOS'
    }

    def 'should yield on empty project detection'() {

        when:
        detector.detectProjectType(getLocalTestProject('empty_project'))

        then:
        def exception = thrown(RuntimeException)
        exception.message == 'No valid project detected'
    }

    private File getLocalTestProject(String fileName) {
        new File(getClass().getResource(fileName).toURI())
    }
}
