package com.apphance.ameba.unit

import com.apphance.ameba.util.file.FileManager
import org.junit.Test

import static org.junit.Assert.assertArrayEquals

class ReadingPackageTest {
    @Test
    public void testReadPackages() {
        File f = new File('src/test/groovy')
        def currentPackage = []
        FileManager.findAllPackages('', f, currentPackage)
        println currentPackage
        String[] packagesList = [
                'com.apphance.ameba.detection',
                'com.apphance.ameba.executor',
                'com.apphance.ameba.executor.linker',
                'com.apphance.ameba.executor.log',
                'com.apphance.ameba.integration.android',
                'com.apphance.ameba.integration.android.apphance',
                'com.apphance.ameba.integration.android.robolectric',
                'com.apphance.ameba.integration.android.robotium',
                'com.apphance.ameba.integration.android.setup',
                'com.apphance.ameba.integration.conventions',
                'com.apphance.ameba.integration.ios',
                'com.apphance.ameba.integration.ios.apphance',
                'com.apphance.ameba.integration.ios.setup',
                'com.apphance.ameba.plugins',
                'com.apphance.ameba.unit',
                'com.apphance.ameba.unit.android',
                'com.apphance.ameba.unit.ios',
                'com.apphance.ameba.util'].sort() as String[]
        currentPackage.sort()
        assertArrayEquals(currentPackage.toString(), packagesList, (String[]) currentPackage)
    }
}
