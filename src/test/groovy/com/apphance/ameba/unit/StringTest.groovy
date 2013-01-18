package com.apphance.ameba.unit

import org.junit.Assert
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * User: opal
 * Date: 18.01.2013
 * Time: 14:12
 */
class StringTest {

    @Test
    void testExtractAndCapitaliseApphanceFramework() {

        def dependency = 'com.apphance:ios.production.armv7:1.8'
        def framework = dependency.split(':')[1].split('\\.')[1]

        assertEquals(framework, 'production')
        assertEquals(framework.replace('p', 'P'), 'Production')

        dependency = 'com.apphance:ios.pre-production.armv7:1.8.1'
        framework = dependency.split(':')[1].split('\\.')[1]

        assertEquals(framework, 'pre-production')
        assertEquals(framework.replace('p', 'P'), 'Pre-Production')
    }
}
