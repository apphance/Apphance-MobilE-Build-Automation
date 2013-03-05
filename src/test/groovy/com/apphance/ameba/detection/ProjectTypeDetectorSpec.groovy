package com.apphance.ameba.detection

import com.apphance.ameba.runBuilds.android.ExecuteAndroidBuildsTest
import com.apphance.ameba.runBuilds.ios.ExecuteIosBuildsTest
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.ameba.util.ProjectType.ANDROID
import static com.apphance.ameba.util.ProjectType.IOS

class ProjectTypeDetectorSpec extends Specification {

    def detector = new ProjectTypeDetector()

    @Unroll
    def 'detects #expectedType project type'() {

        expect:
        detector.detectProjectType(path) == expectedType

        where:
        expectedType | path
        ANDROID      | ExecuteAndroidBuildsTest.testProject
        IOS          | ExecuteIosBuildsTest.testProjectOneVariant
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
