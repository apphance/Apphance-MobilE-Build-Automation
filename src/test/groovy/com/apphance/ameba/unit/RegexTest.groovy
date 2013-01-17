package com.apphance.ameba.unit

import org.junit.Assert
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 * User: opal
 * Date: 17.01.2013
 * Time: 11:33
 */
class RegexTest {

    @Test
    public void testAndroidApphanceLibJarPattern() {
        def JAR_PATTERN = ".*android\\.(pre\\-)?production\\-(\\d+\\.)+\\d+\\.jar"
        assertTrue("android.production-1.8.jar".matches(JAR_PATTERN))
        assertTrue("android.pre-production-1.8.1.jar".matches(JAR_PATTERN))
        assertFalse("android.production-1.8jar".matches(JAR_PATTERN))
        assertFalse("android.production-1.8..jar".matches(JAR_PATTERN))
        assertFalse("android.pre-production-1.jar".matches(JAR_PATTERN))


    }
}
