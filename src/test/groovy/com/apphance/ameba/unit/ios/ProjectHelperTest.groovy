package com.apphance.ameba.unit.ios

import com.apphance.ameba.ProjectHelper
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals

class ProjectHelperTest {

    private ProjectHelper ph = new ProjectHelper()

    @Test
    void testBasicJenkinsUrl() {

        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/android"))
        Project project = projectBuilder.build()
        def map = [JENKINS_URL: 'http://example.com/jenkins',
                JOB_URL: 'http://example.com/jenkins/test/job',
                WORKSPACE: new File("testProjects").getCanonicalFile().getParentFile().getCanonicalPath()]
        assertEquals('http://example.com/jenkins/test/job/ws/testProjects/android',
                ph.getJenkinsURL(project, map))
    }

    @Test
    void testJenkinsUrlWithSlash() {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/android"))
        Project project = projectBuilder.build()
        def map = [JENKINS_URL: 'http://example.com/jenkins',
                JOB_URL: 'http://example.com/jenkins/test/job/',
                WORKSPACE: new File("testProjects").getCanonicalFile().getParentFile().getCanonicalPath()]
        assertEquals('http://example.com/jenkins/test/job/ws/testProjects/android',
                ph.getJenkinsURL(project, map))
    }
}
