package com.apphance.ameba.unit

import com.apphance.ameba.ProjectHelper
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue;

class RemoveSymlinksTest {
    @Test
    public void removeSymlinks() throws Exception {
        ProjectHelper ph = new ProjectHelper()
        File currentDir = new File("testProjects/android")
        [
                'ln',
                '-s',
                'missingFile',
                'missingFileLink',
        ].execute([], currentDir).waitFor()
        assertTrue(currentDir.list().any { it == 'missingFileLink' })
        assertFalse(new File(currentDir, "missingFileLink").canonicalFile.exists())
        ph.removeMissingSymlinks(currentDir)
        assertFalse(currentDir.list().any { it == 'missingFileLink' })
        assertFalse(new File(currentDir, "missingFileLink").canonicalFile.exists())
    }
}
