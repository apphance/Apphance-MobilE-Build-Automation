package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.build.IOSPlugin;
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkPlugin;

class TestBasicIOSTasks extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(IOSFrameworkPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'clean',
            'buildAll',
            'buildAllSimulators',
            'buildFramework',
            'build-GradleXCode-BasicConfiguration',
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
			'readIOSProjectConfiguration',
			'readIOSProjectTargetAndConfiguration',
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
			'prepareReleaseSetup',
			'prepareIOSSetup',
            'verifyBaseSetup',
            'verifySetup',
            'verifyIOSSetup',
			'verifyReleaseSetup',
			'showBaseProperties',
			'showProperties',
			'showIOSProperties',
			'showReleaseProperties'
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
    }
}
