package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.junit.Test

import com.apphance.ameba.ios.plugins.apphance.IOSApphanceSourceHelper

class IOSApphanceSourceHelperTest {
    private void baseAddApphanceInitBraceInLine(File baseDir) throws Exception {
        IOSApphanceSourceHelper helper = new IOSApphanceSourceHelper()
        File f = new File(baseDir, "MoviePlayerAppDelegate.m")
        File temp = new File(baseDir, "MoviePlayerAppDelegate.m.orig")
        temp.delete()
        temp.text = f.text
        try {
            helper.addApphanceInit(baseDir,"TESTKEY")
            assertTrue(f.text.contains("TESTKEY"))
        } finally {
            f.delete()
            f.text = temp.text
            temp.delete()
        }
    }


    @Test
    public void testAddApphanceInitBracketNotInLine() throws Exception {
        File baseDir = new File("testProjects/ios-add-apphance/bracketNotInLine/")
        baseAddApphanceInitBraceInLine(baseDir)
    }

    @Test
    public void testAddApphanceInitBraceInLine() throws Exception {
        File baseDir = new File("testProjects/ios-add-apphance/bracketInLine/")
        baseAddApphanceInitBraceInLine(baseDir)
    }

}
