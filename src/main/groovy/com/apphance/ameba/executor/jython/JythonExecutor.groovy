package com.apphance.ameba.executor.jython

import com.apphance.ameba.util.Preconditions
import org.python.core.PyException
import org.python.core.PyString
import org.python.core.PySystemState
import org.python.util.PythonInterpreter

@Mixin(Preconditions)
class JythonExecutor {

    List<String> executeScript(String filename, List<String> arguments = []) {

        validate(filename != null && !filename.trim().empty, {
            throw new IllegalArgumentException('Script name must not be empty!')
        })

        def output = new ByteArrayOutputStream()
        try {

            def script = loadScript(filename)
            def systemState = new PySystemState()

            addArgumentsToSystem(systemState, arguments)


            def interpreter = new PythonInterpreter(null, systemState)
            interpreter.setOut(output)
            interpreter.execfile(script)
            interpreter.cleanup()
        } catch (Exception e) {
            String msg = e instanceof PyException ? ((PyException) e).value : e.message
            throw new JythonExecutorException(msg, filename, arguments)
        }
        output.toString().split('\n')
    }

    private InputStream loadScript(String filename) {
        getClass().getResource(filename).openStream()
    }

    private void addArgumentsToSystem(PySystemState systemState, List<String> commandArguments) {
        commandArguments.each {
            systemState.argv.append(new PyString(it))
        }
    }
}
