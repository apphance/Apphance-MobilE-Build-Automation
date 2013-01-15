package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.junit.Test

class TestBasicBuildTasks extends BaseAndroidTaskTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        project.project.plugins.apply(AndroidPlugin.class)
    }

    @Test
    public void testTestTasksAvailable() {
        verifyTasksInGroup(getProject(), ['checkTests'], AmebaCommonBuildTaskGroups.AMEBA_TEST)
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(), [], AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testConfigurationTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'cleanConfiguration',
                'readProjectConfiguration',
        ], AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'prepareSetup',
                'verifySetup',
                'showConventions',
                'showSetup',
        ], AmebaCommonBuildTaskGroups.AMEBA_SETUP)
    }
}
