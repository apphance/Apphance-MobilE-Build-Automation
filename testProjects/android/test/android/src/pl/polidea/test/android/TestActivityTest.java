package com.apphance.amebaTest.android;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.apphance.amebaTest.android.TestActivityTest \
 * com.apphance.amebaTest.android.tests/android.test.InstrumentationTestRunner
 */
public class TestActivityTest extends ActivityInstrumentationTestCase2<TestActivity> {

    public TestActivityTest() {
        super("com.apphance.amebaTest.android", TestActivity.class);
    }

    public void testEmpty() throws Exception {
        assertTrue(true);
    }

}
