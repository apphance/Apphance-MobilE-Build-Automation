package com.apphance.ameba.applyPlugins.ios

import com.apphance.ameba.ios.plugins.ocunit.IOSUnitTestPlugin
import org.junit.Test

class TestOCUnitTasks extends AbstractBaseIOSTaskTest {

    @Test
    public void testCOCUnitTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'runUnitTests',
        ], IOSUnitTestPlugin.AMEBA_IOS_UNIT)
    }
}
