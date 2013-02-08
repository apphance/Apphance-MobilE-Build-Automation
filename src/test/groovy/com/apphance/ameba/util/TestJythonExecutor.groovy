package com.apphance.ameba.util

import com.apphance.ameba.util.jython.JythonExecutor
import org.junit.Assert
import org.junit.Test

class TestJythonExecutor {


    @Test
    public void testRunScript() {
        def executor = new JythonExecutor()
        def input = ['a', 'b', 'c']
        def baos = new ByteArrayOutputStream()
        executor.executeScript('test_script.py', input, baos)
        def output = baos.toString().trim()
        Assert.assertEquals("['', 'a', 'b', 'c']",output)
    }
}
