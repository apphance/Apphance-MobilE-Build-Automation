package com.apphance.ameba.unit.ios

import com.apphance.ameba.util.file.FileManager
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class SortingDirectoriesTest {

    @Test
    public void testSortingDirectoriesWorksInSimpleCaseOnFiles() throws Exception {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File("testProjects/ios/GradleXCode"))
        Project project = projectBuilder.build()
        List res = FileManager.getDirectoriesSortedAccordingToDepth(project, { true })
        List newRes = res.collect { sprintf("%08d", it.findAll('[/\\\\]').size()) }
        int last = 0
        newRes.each {
            int current = Integer.parseInt(it)
            assertTrue(current >= last)
            last = current
        }
    }
}
