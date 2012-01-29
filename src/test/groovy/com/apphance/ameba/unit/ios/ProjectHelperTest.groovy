package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import com.apphance.ameba.ProjectHelper

class ProjectHelperTest {

    @Test
    void testBasicJenkinsUrl() {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/android"))
        Project project = projectBuilder.build()
        def map = [JENKINS_URL : 'http://example.com/jenkins',
                    JOB_URL : 'http://example.com/jenkins/test/job',
                    WORKSPACE:  new File("testProjects").getCanonicalFile().getParentFile().getCanonicalPath()]
        assertEquals('http://example.com/jenkins/test/job/ws/testProjects/android',
                new ProjectHelper().getJenkinsURL(project, map))
    }
    @Test
    void testJenkinsUrlWithSlash() {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/android"))
        Project project = projectBuilder.build()
        def map = [JENKINS_URL : 'http://example.com/jenkins',
                    JOB_URL : 'http://example.com/jenkins/test/job/',
                    WORKSPACE:  new File("testProjects").getCanonicalFile().getParentFile().getCanonicalPath()]
        assertEquals('http://example.com/jenkins/test/job/ws/testProjects/android',
                new ProjectHelper().getJenkinsURL(project, map))
    }
}
