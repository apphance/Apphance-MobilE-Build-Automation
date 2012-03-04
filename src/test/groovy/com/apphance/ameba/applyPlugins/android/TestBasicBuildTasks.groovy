package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin


class TestBasicBuildTasks extends BaseAndroidTaskTest{

    @Override
    protected void setUp() throws Exception {
        super.setUp()
        project.project.plugins.apply(AndroidPlugin.class)
    }

    @Test
    public void testTestTasksAvailable() {
        verifyTasksInGroup(getProject(),['checkTests'],AmebaCommonBuildTaskGroups.AMEBA_TEST)
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
        ],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }
    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareSetup',
            'verifySetup',
            'showSetup',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
        assertEquals(['PrepareBaseSetupOperation'], project.prepareSetup.prepareSetupOperations.collect { it.class.simpleName } )
        assertEquals(['VerifyBaseSetupOperation'], project.verifySetup.verifySetupOperations.collect { it.class.simpleName } )
        assertEquals(['ShowBaseSetupOperation'], project.showSetup.showSetupOperations.collect { it.class.simpleName } )
    }
}
