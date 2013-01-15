package com.apphance.ameba.unit

import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import org.junit.Test

import static org.junit.Assert.assertEquals

class ReleaseCategoryTest {
    @Test
    public void testExtUrl() throws Exception {
        def baseUrl, directory
        (baseUrl, directory) = ProjectReleaseCategory.splitUrl("http://www.example.com/ext/test")
        assertEquals(new URL("http://www.example.com/ext/"), baseUrl)
        assertEquals("test", directory)
    }

    @Test
    public void testBaseUrl() throws Exception {
        def baseUrl, directory
        (baseUrl, directory) = ProjectReleaseCategory.splitUrl("http://www.example.com/test")
        assertEquals(new URL("http://www.example.com/"), baseUrl)
        assertEquals("test", directory)
    }

    @Test
    public void testSlashEndingUrl() throws Exception {
        def baseUrl, directory
        (baseUrl, directory) = ProjectReleaseCategory.splitUrl("http://www.example.com/test/")
        assertEquals(new URL("http://www.example.com/"), baseUrl)
        assertEquals("test", directory)
    }
}
