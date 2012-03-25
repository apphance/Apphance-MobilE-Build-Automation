package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.Test

import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.unit.EmmaDumper

class IOSProjectConfigurationTest {
    @Test
    void testSimpleToString() {
        def x = IOSProjectConfiguration [
                            distributionDirectory:new java.io.File('dist'),
                            targets:[
                                "target1",
                                "target2"
                            ]]
        x.toString()
    }

    @AfterClass
    static public void afterClass() {
        EmmaDumper.dumpEmmaCoverage()
    }
}
