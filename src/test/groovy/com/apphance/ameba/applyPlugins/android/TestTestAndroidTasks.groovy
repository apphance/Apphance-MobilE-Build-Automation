package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.junit.Test

class TestTestAndroidTasks extends BaseAndroidTaskTest {

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(true), [
                'checkTests',
                'testAndroid',
                'cleanAVD',
                'createAVD',
                'startEmulator',
                'stopAllEmulators',
                'prepareRobotium',
                'prepareRobolectric',
                'testRobolectric'
        ], AmebaCommonBuildTaskGroups.AMEBA_TEST)
    }
}
