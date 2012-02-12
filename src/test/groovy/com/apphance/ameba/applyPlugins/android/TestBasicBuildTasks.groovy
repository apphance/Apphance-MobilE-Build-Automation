package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.build.AndroidPlugin


class TestBasicBuildTasks extends BaseAndroidTaskTest{

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        project.project.plugins.apply(AndroidPlugin.class)
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),['checkTests'],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testConfigurationTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanConfiguration',
            'copyGalleryFiles',
            'readProjectConfiguration',
            'showProjectConfiguration',
        ],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }
    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareBaseSetup',
            'prepareSetup',
            'verifyBaseSetup',
            'verifySetup',
            'showBaseSetup',
            'showSetup',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
    }
}
