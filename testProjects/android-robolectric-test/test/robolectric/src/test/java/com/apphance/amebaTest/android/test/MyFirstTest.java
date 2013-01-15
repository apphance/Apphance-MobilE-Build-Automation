package com.apphance.amebaTest.android;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MyFirstTest {
    @Test
    public void myFirstRobolectricTest() {
        assertTrue(true);
    }
}
