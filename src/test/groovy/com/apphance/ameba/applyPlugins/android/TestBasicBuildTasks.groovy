package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups


class TestBasicBuildTasks extends BaseAndroidTaskTest{

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),['checkTests'],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),['cleanRelease'],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testConfigurationTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanConfiguration',
            'copyGalleryFiles',
            'readProjectConfiguration',
            'showProjectConfiguration',
            'verifyReleaseNotes'
        ],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }
    @Test
    public void testMessagingTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'sendMailMessage'
        ],AmebaCommonBuildTaskGroups.AMEBA_MESSAGING)
    }

    @Test
    public void testReportingTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildSourcesZip',
            'prepareImageMontage'
        ],AmebaCommonBuildTaskGroups.AMEBA_REPORTS)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareBaseSetup',
            'prepareSetup',
            'prepareReleaseSetup',
            'verifyBaseSetup',
            'verifySetup',
            'showBaseProperties',
            'verifyReleaseSetup',
            'showSetup',
            'showReleaseSetup',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
    }
}
