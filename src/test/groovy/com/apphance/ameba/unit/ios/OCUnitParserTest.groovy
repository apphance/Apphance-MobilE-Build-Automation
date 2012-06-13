package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.Test

import com.apphance.ameba.ios.plugins.ocunit.OCUnitParser

class OCUnitParserTest {

    String TEST_RESULT = '''
MapTiles/MapTiles.128.LoDPI.sqlitedb:  tile data will not be cached
Run test suite /Users/potiuk/Documents/workspace/SomeApp/build/Debug-iphonesimulator/UnitTests.octest(Tests)
Test Suite '/Users/potiuk/Documents/workspace/SomeApp/build/Debug-iphonesimulator/UnitTests.octest(Tests)' started at 2011-10-03 00:24:38 +0000
Run test suite SMSomeTest
Test Suite 'SMSomeTest' started at 2011-10-03 00:24:38 +0000
Run test case testSomeDataParsing
Test Case '-[SMSomeTest testSomeDataParsing]' started.
2011-10-03 01:24:38.752 otest[9539:3b03] Created new object of class SMSome with id ddavis in context 129772080
Test Case '-[SMSomeTest testSomeDataParsing]' passed (0.048 seconds).

Test Suite 'SMSomeTest' finished at 2011-10-03 00:24:38 +0000.
Executed 1 test, with 0 failures (0 unexpected) in 0.048 (0.053) seconds


Run test suite SMCoreDataHandlerTest
Test Suite 'SMCoreDataHandlerTest' started at 2011-10-03 00:24:38 +0000
Run test case testGeneralHandlerCreatesObjectWithId
Test Case '-[SMCoreDataHandlerTest testGeneralHandlerCreatesObjectWithId]' started.
2011-10-03 01:24:38.760 otest[9539:3b03] Created new object of class SMSomeHandler
2011-10-03 01:24:38.823 otest[9539:3b03] Created new object of class SMSettings with id SMDefaultCredentialsKey in context 115354192
2011-10-03 01:24:38.830 otest[9539:3b03] No new object found - creating new one {
    SMSomeHandler = "<SMSomeHandler: 0xede750>";
} SMSomeHandler
2011-10-03 01:24:38.842 otest[9539:3b03] Created new object of class SMSome with id dummyEntityId in context 115354192
2011-10-03 01:24:38.846 otest[9539:3b03] Deallocing object <SMSomeHandler: 0xede750>
Test Case '-[SMCoreDataHandlerTest testGeneralHandlerCreatesObjectWithId]' passed (0.088 seconds).

Run test case testGeneralHandlerIsMonostate
Test Case '-[SMCoreDataHandlerTest testGeneralHandlerIsMonostate]' started.
2011-10-03 01:24:38.853 otest[9539:3b03] Created new object of class SMSomeHandler
2011-10-03 01:24:38.857 otest[9539:3b03] No new object found - creating new one {
    SMSomeHandler = "<SMSomeHandler: 0xeee930>";
} SMSomeHandler
2011-10-03 01:24:38.860 otest[9539:3b03] Existing object found... Releasing new one and returning old one
Test Case '-[SMCoreDataHandlerTest testGeneralHandlerIsMonostate]' passed (0.008 seconds).

Run test case testGeneralHandlerUniqueKeyImplementation
Test Case '-[SMCoreDataHandlerTest testGeneralHandlerUniqueKeyImplementation]' started.
2011-10-03 01:24:38.864 otest[9539:3b03] Existing object found... Releasing new one and returning old one
Test Case '-[SMCoreDataHandlerTest testGeneralHandlerUniqueKeyImplementation]' passed (0.007 seconds).

Test Suite 'SMCoreDataHandlerTest' finished at 2011-10-03 00:24:38 +0000.
Executed 3 tests, with 0 failures (0 unexpected) in 0.103 (0.112) seconds


Run test suite SMModelSharedTest
Test Suite 'SMModelSharedTest' started at 2011-10-03 00:24:38 +0000
Run test case testSharedInstanceIsASingleton
Test Case '-[SMModelSharedTest testSharedInstanceIsASingleton]' started.
Test Case '-[SMModelSharedTest testSharedInstanceIsASingleton]' passed (0.000 seconds).

Run test case testSharedModelIsAlwaysSingleInstance
Test Case '-[SMModelSharedTest testSharedModelIsAlwaysSingleInstance]' started.
Test Case '-[SMModelSharedTest testSharedModelIsAlwaysSingleInstance]' passed (0.000 seconds).

Test Suite 'SMModelSharedTest' finished at 2011-10-03 00:24:38 +0000.
Executed 2 tests, with 0 failures (0 unexpected) in 0.000 (0.000) seconds


Run test suite UnitTests
Test Suite 'UnitTests' started at 2011-10-03 00:24:38 +0000
Run test case testPassingForJarek
Test Case '-[UnitTests testPassingForJarek]' started.
Test Case '-[UnitTests testPassingForJarek]' passed (0.000 seconds).

Test Suite 'UnitTests' finished at 2011-10-03 00:24:38 +0000.
Executed 1 test, with 0 failures (0 unexpected) in 0.000 (0.000) seconds


Test Suite '/Users/potiuk/Documents/workspace/SomeApp/build/Debug-iphonesimulator/UnitTests.octest(Tests)' finished at 2011-10-03 00:24:38 +0000.
Executed 7 tests, with 0 failures (0 unexpected) in 0.151 (0.207) seconds


/Developer/Tools/RunPlatformUnitTests.include:446: note: Passed tests for architecture 'i386' (GC OFF)

/Developer/Tools/RunPlatformUnitTests.include:462: note: Completed tests for architectures 'i386'

Touch build/Debug-iphonesimulator/UnitTests.octest
    cd /Users/potiuk/Documents/workspace/SomeApp
    setenv PATH "/Developer/Platforms/iPhoneSimulator.platform/Developer/usr/bin:/Developer/usr/bin:/data/gradle/bin:/Library/Frameworks/Python.framework/Versions/2.7/bin:/opt/local/bin:/opt/local/sbin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/X11/bin:/Users/potiuk/bin:/Applications/android-sdk-mac_x86/tools:/Applications/android-sdk/tools:/Applications/android-sdk/platform-tools:/Volumes/Android/build:/Library/Groovy/groovy/bin"
    /usr/bin/touch -c /Users/potiuk/Documents/workspace/SomeApp/build/Debug-iphonesimulator/UnitTests.octest
'''
    @Test
    void testParsingOutput() {
        OCUnitParser parser= new OCUnitParser()
        parser.parse(TEST_RESULT.split('\n') as List)
        assertEquals([
            'SMSomeTest',
            'SMCoreDataHandlerTest',
            'SMModelSharedTest',
            'UnitTests'
        ], parser.testSuites.collect {it.name})
    }
}
