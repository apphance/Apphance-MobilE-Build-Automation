package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.junit.Test

class TestApphanceAndroidTasks extends BaseAndroidTaskTest {

    @Test
    public void testReportsTasksAvailable() {
        verifyTasksInGroup(getProject(false), [
                'convertLogsToAndroid',
                'convertLogsToApphance',
                'removeApphanceFromManifest',
                'uploadDebug'
        ], AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE)
    }
}
