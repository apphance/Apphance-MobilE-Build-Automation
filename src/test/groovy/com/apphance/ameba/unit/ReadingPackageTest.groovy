package com.apphance.ameba.unit;

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.Test

import com.apphance.ameba.ProjectHelper
class ReadingPackageTest {
    @Test
    public void testReadPackages() {
        ProjectHelper ph = new ProjectHelper()
        File f = new File("src/test/groovy")
        def currentPackage = []
        ph.findAllPackages("", f, currentPackage)
		currentPackage.sort()
		String[] packagesList = (String [])[
            'com.apphance.ameba',
            'com.apphance.ameba.apphance.android',
            'com.apphance.ameba.applyPlugins.android',
            'com.apphance.ameba.applyPlugins.ios',
            'com.apphance.ameba.applyPlugins.vcs',
            'com.apphance.ameba.conventions',
            'com.apphance.ameba.documentation',
            'com.apphance.ameba.runBuilds.android',
            'com.apphance.ameba.runBuilds.ios',
            'com.apphance.ameba.unit',
			'com.apphance.ameba.unit.ios',
			'com.apphance.ameba.unit.vcs',
			'com.apphance.ameba.unit.android',
            'com.apphance.ameba.unit.apphance.android',
        ].sort()
        assertArrayEquals(currentPackage.toString(), packagesList, (String[]) currentPackage)
    }

    @AfterClass
    static public void afterClass() {
        EmmaDumper.dumpEmmaCoverage()
    }
}
