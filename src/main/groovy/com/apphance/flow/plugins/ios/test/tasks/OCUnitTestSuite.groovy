package com.apphance.flow.plugins.ios.test.tasks

import static com.apphance.flow.plugins.ios.test.tasks.OCUnitTestResult.FAILURE
import static com.apphance.flow.plugins.ios.test.tasks.OCUnitTestResult.SUCCESS

class OCUnitTestSuite {
    String name
    String startTimestamp
    String endTimestamp
    Collection<OCUnitTestSuite> testSuites = []
    Collection<OCUnitTestCase> testCases = []

    int getFailureCount() {
        int count = 0;
        testSuites.each { testSuite -> count += testSuite.failureCount }
        return count + testCases.findAll { it.result == FAILURE }.size()
    }

    int getSuccessCount() {
        int count = 0;
        testSuites.each { testSuite -> count += testSuite.successCount }
        return count + testCases.findAll { it.result == SUCCESS }.size()
    }

    double getDuration() {
        double duration = 0.0
        testSuites.each { testSuite -> duration += testSuite.duration }
        testCases.each { duration += it.duration }
        return duration
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TestSuite [name=");
        builder.append(name);
        builder.append(", \nstartTimestamp=");
        builder.append(startTimestamp);
        builder.append(", \nendTimestamp=");
        builder.append(endTimestamp);
        builder.append(", \ntestSuites=");
        builder.append(testSuites);
        builder.append(", \ntestCases=");
        builder.append(testCases);
        builder.append("]");
        return builder.toString();
    }
}
