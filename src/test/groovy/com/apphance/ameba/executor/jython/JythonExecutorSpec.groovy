package com.apphance.ameba.executor.jython

import spock.lang.Specification

class JythonExecutorSpec extends Specification {

    def 'jython executor runs correctly'() {
        given:
        def executor = new JythonExecutor()
        and:
        def input = ['a', 'b', 'c']

        when:
        def output = executor.executeScript('test_script.py', input)
        then:
        "['', 'a', 'b', 'c']" == output[0]
    }

    def 'jython executor fails when wrong script name passed'() {
        given:
        def executor = new JythonExecutor()

        when:
        executor.executeScript(' ')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Script name must not be empty!'
    }

    def 'jython executor fails with buggy script'() {
        given:
        def executor = new JythonExecutor()
        def filename = 'test_failed_script.py'
        when:
        executor.executeScript(filename)

        then:
        def e = thrown(JythonExecutorException)
        e.message == "'<reflected field public org.python.core.PyObject o' object has no attribute 'argvasdasasf'"
        e.filename == filename
        e.args == []
    }
}
