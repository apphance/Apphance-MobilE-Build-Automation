package com.apphance.ameba.runBuilds.android;

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.Test

import com.apphance.ameba.unit.EmmaDumper

class ExecuteAndroidIsInPath {
    @Test
    void testAndroidIsInPath () {
        Process proc = Runtime.getRuntime().exec('android --help')
        proc.waitFor()
        proc.in.eachLine {line -> println line }
        println proc.err.text
        proc.exitValue()
    }

    @AfterClass
    static public void afterClass() {
        EmmaDumper.dumpEmmaCoverage()
    }
}
