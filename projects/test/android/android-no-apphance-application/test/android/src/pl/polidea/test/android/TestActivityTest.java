package com.apphance.flowTest.android;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.apphance.flowTest.android.TestActivityTest \
 * com.apphance.flowTest.android.tests/android.test.InstrumentationTestRunner
 */
public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    public TestActivityTest() {
        super("com.apphance.flowTest.android", TestActivity.class);
    }

    public void testEmpty() throws Exception {
        assertTrue(true);
    }

}
