package com.apphance.flow.plugins.ios.test.tasks.results.parser

class OCUnitTestError {
    String file
    int line
    String errorMessage

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TestError [file=");
        builder.append(file);
        builder.append(", \nline=");
        builder.append(line);
        builder.append(", \nerrorMessage=");
        builder.append(errorMessage);
        builder.append("]");
        return builder.toString();
    }
}
