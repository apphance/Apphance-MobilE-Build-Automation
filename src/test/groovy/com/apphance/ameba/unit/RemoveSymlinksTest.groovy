package com.apphance.ameba.unit

import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.util.file.FileManager
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
        FileManager.removeMissingSymlinks(currentDir)
        assertFalse(currentDir.list().any { it == 'missingFileLink' })
        assertFalse(new File(currentDir, "missingFileLink").canonicalFile.exists())
    }
}
