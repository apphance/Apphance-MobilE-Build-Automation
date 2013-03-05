package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.junit.Test

class TestReleaseAndroidTasks extends BaseAndroidTaskTest {

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(false), [
                'cleanRelease',
                'updateVersion',
                'buildDocumentationZip',
                'buildSourcesZip',
                'prepareAvailableArtifactsInfo',
                'prepareForRelease',
                'prepareImageMontage',
                'prepareMailMessage',
                'sendMailMessage',
                'verifyReleaseNotes'
        ], AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }
}
