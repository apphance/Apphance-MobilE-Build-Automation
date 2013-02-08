package com.apphance.ameba.util.jython

import org.python.core.PyString
import org.python.core.PySystemState
import org.python.util.PythonInterpreter

class JythonExecutor {

    def executeScript(name, commandArguments, output = System.out) {

        def script = loadScript(name)

        def systemState = new PySystemState()
        addArgumentsToSystem(systemState, commandArguments)

        def interpreter = new PythonInterpreter(null, systemState)
        interpreter.out = output
        interpreter.execfile(script)
        interpreter.cleanup()
    }

    def loadScript(name) {
        this.class.classLoader.getResourceAsStream(name)
    }

    def addArgumentsToSystem(systemState, commandArguments) {
        commandArguments.each {
            systemState.argv.append(new PyString(it))
        }
    }
}
