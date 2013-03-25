package com.apphance.ameba.plugins.ios.ocunit.tasks
/**
 * Test Case POJO.
 *
 */
class OCUnitTestCase {
    String name
    double duration
    OCUnitTestResult result
    Collection<OCUnitTestError> errors = []

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TestCase [name=");
        builder.append(name);
        builder.append(", \nduration=");
        builder.append(duration);
        builder.append(", \nresult=");
        builder.append(result);
        builder.append(", \nerrors=");
        builder.append(errors);
        builder.append("]");
        return builder.toString();
    }
}
