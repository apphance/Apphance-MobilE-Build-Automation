package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.IOSPlugin

class TestBasicIOSTasks extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'clean',
            'buildAll',
            'buildAllSimulators',
            'buildFramework',
            'buildSingleRelease',
            'checkTests',
            'copyMobileProvision',
            'replaceBundleIdPrefix',
            'unlockKeyChain'
        ],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }

    @Test
    public void testConfigurationTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanConfiguration',
            'copyGalleryFiles',
            'readProjectConfiguration',
            'readIOSProjectVersions',
            'showProjectConfiguration',
            'verifyReleaseNotes'
        ],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanRelease',
            'preRelease',
            'updateVersion',
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareBaseSetup',
            'prepareSetup',
            'verifyBaseSetup',
            'verifySetup',
            'verifyIOSSetup',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
    }
}
