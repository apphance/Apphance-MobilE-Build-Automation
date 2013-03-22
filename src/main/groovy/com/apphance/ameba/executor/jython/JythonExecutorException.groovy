package com.apphance.ameba.executor.jython

class JythonExecutorException extends RuntimeException {

    private String filename
    private List<String> args

    JythonExecutorException(String s, String filename, List<String> args = []) {
        super(s)
        this.filename = filename
        this.args = args
    }

    String getFilename() {
        return filename
    }

    List<String> getArgs() {
        return args
    }
}
