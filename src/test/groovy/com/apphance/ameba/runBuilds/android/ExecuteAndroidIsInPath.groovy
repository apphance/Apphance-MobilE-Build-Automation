package com.apphance.ameba.runBuilds.android

import org.junit.Test

class ExecuteAndroidIsInPath {
    @Test
    void testAndroidIsInPath() {
        Process proc = Runtime.getRuntime().exec('android --help')
        proc.waitFor()
        proc.in.eachLine { line -> println line }
        println proc.err.text
        proc.exitValue()
    }
}
