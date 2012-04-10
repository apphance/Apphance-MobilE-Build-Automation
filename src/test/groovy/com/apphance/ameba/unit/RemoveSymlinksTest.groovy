package com.apphance.ameba.unit;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import com.apphance.ameba.ProjectHelper;

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
        ].execute([], currentDir)
        def l = currentDir.list()
        assertTrue(currentDir.list().any { it == 'missingFileLink'})
        assertFalse(new File(currentDir,"missingFileLink").canonicalFile.exists())
        ph.removeMissingSymlinks(currentDir)
        assertFalse(currentDir.list().any { it == 'missingFileLink'})
        assertFalse(new File(currentDir,"missingFileLink").canonicalFile.exists())
    }

    @AfterClass
    static public void afterClass() {
        EmmaDumper.dumpEmmaCoverage()
    }
}
