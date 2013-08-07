package com.apphance.flow.plugins.ios.test.tasks

import static org.gradle.api.logging.Logging.getLogger

/**
 * Parses text output of OCUNIT.
 *
 */
class OCUnitParser {

    private logger = getLogger(getClass())

    static def TEST_STARTED = ~/^\s*Test\s+Suite\s+'\s*(\S.+(?:\.octest).*\S)\s*'\s+started\s+at\s+(\S.+\S)\s*$/
    static def TEST_FINISHED = ~/^\s*Test\s+Suite\s+'\s*(\S.+(?:\.octest).*\S)\s*'\s+finished\s+at\s+(\S.+\S)\s*$/
    static def TEST_SUITE_STARTED = ~/^\s*Test\s+Suite\s+'\s*(\S.+\S)\s*'\s+started\s+at\s+(\S.+\S)\s*$/
    static def TEST_SUITE_FINISHED = ~/^\s*Test\s+Suite\s+'\s*(\S.+\S)\s*'\s+finished\s+at\s+(\S.+\S)\s*\.\s*$/
    static def TEST_CASE_STARTED = ~/^\s*Test\s+Case\s+'-\[\s*(\S.+\S)\s*\]'\s+started\s*\.\s*$/
    static def TEST_CASE_PASSED = ~/^\s*Test\s+Case\s+'-\[\s*(\S.+\S)\s*\]'\s+passed\s+\(\s*([0-9.]+)\s+seconds\s*\)\s*\.\s*$/
    static def TEST_CASE_FAILED = ~/^\s*Test\s+Case\s+'-\[\s*(\S.+\S)\s*\]'\s+failed\s+\(\s*([0-9.]+)\s+seconds\s*\)\s*\.\s*$/
    static def TEST_CASE_ERROR = ~/^\s*(\S.+\S)\s*:\s*(\d+)\s*:\s+error:\s+-\[\s*(\S.+\S)\s*\]\s+:\s+(\S.+\S)\s*$/

    private List<OCUnitTestSuite> suiteStack = new LinkedList<OCUnitTestSuite>()

    Collection<OCUnitTestSuite> testSuites = []

    void parse(Collection<String> lines) {
        lines.each(doMatch)
    }

    def doMatch = { line ->
        def testStartedMatcher = TEST_STARTED.matcher(line)
        OCUnitTestSuite currentSuite = suiteStack.empty ? null : suiteStack[-1]
        if (testStartedMatcher.matches()) {
            logger.info("Starting test .... ${testStartedMatcher[0][1]}")
            return
        }
        def testFinishedMatcher = TEST_FINISHED.matcher(line)
        if (testFinishedMatcher.matches()) {
            logger.info("Finishing test .... ${testFinishedMatcher[0][1]}")
            return
        }
        def testSuiteStartedMatcher = TEST_SUITE_STARTED.matcher(line)
        if (testSuiteStartedMatcher.matches()) {
            logger.info("Starting test suite .... ${testSuiteStartedMatcher[0][1]}")
            OCUnitTestSuite ts = new OCUnitTestSuite()
            ts.name = testSuiteStartedMatcher[0][1]
            ts.startTimestamp = testSuiteStartedMatcher[0][2]
            if (currentSuite == null) {
                testSuites << ts
            } else {
                currentSuite.testSuites << ts
            }
            suiteStack.push(ts)
            return
        }
        def testSuiteFinishedMatcher = TEST_SUITE_FINISHED.matcher(line)
        if (testSuiteFinishedMatcher.matches()) {
            logger.info("Finishing test suite .... ${testSuiteFinishedMatcher[0][1]}")
            assert currentSuite.name == testSuiteFinishedMatcher[0][1]
            currentSuite.endTimestamp = testSuiteFinishedMatcher[0][2]
            suiteStack.pop()
        }
        def testCaseStartedMatcher = TEST_CASE_STARTED.matcher(line)
        if (testCaseStartedMatcher.matches()) {
            logger.info("Starting test case .... ${testCaseStartedMatcher[0][1]}")
            assert testCaseStartedMatcher[0][1].startsWith(currentSuite.name)
            OCUnitTestCase tc = new OCUnitTestCase()
            tc.name = testCaseStartedMatcher[0][1].substring(currentSuite.name.length() + 1)
            currentSuite.testCases << tc
            return
        }
        def testCasePassedMatcher = TEST_CASE_PASSED.matcher(line)
        if (testCasePassedMatcher.matches()) {
            logger.info("Test case passed .... ${testCasePassedMatcher[0][1]}")
            assert testCasePassedMatcher[0][1].contains(currentSuite.testCases[-1].name)
            currentSuite.testCases[-1].result = OCUnitTestResult.SUCCESS
            currentSuite.testCases[-1].duration = Double.parseDouble(testCasePassedMatcher[0][2])
            return
        }
        def testCaseFailedMatcher = TEST_CASE_FAILED.matcher(line)
        if (testCaseFailedMatcher.matches()) {
            logger.info("Test case failed .... ${testCaseFailedMatcher[0][1]}")
            assert testCaseFailedMatcher[0][1].contains(currentSuite.testCases[-1].name)
            currentSuite.testCases[-1].result = OCUnitTestResult.FAILURE
            currentSuite.testCases[-1].duration = Double.parseDouble(testCaseFailedMatcher[0][2])
            return
        }
        def testCaseErrorMatcher = TEST_CASE_ERROR.matcher(line)
        if (testCaseErrorMatcher.matches()) {
            logger.info("Test case error .... ${testCaseErrorMatcher[0][1]}")
            assert testCaseErrorMatcher[0][3].contains(currentSuite.testCases[-1].name)
            currentSuite.testCases[-1].result = OCUnitTestResult.FAILURE
            OCUnitTestError te = new OCUnitTestError()
            te.file = testCaseErrorMatcher[0][1]
            te.line = Integer.parseInt(testCaseErrorMatcher[0][2])
            te.errorMessage = testCaseErrorMatcher[0][4]
            currentSuite.testCases[-1].errors << te
        }
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Parser [testSuites=");
        builder.append(testSuites);
        builder.append(", \nsuiteStack=");
        builder.append(suiteStack);
        builder.append("]");
        return builder.toString();
    }
}
