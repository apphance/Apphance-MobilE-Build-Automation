package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin;

class TestBasicIOSTasks extends AbstractBaseIOSTaskTest {

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
            'build-GradleXCode-BasicConfiguration',
            'buildSingleRelease',
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
            'readIOSParametersFromXcode',
            'readIOSProjectVersions',
            'showProjectConfiguration'
        ],AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareSetup',
            'verifySetup',
            'showSetup',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
        assertEquals([
            'PrepareBaseSetupOperation',
            'PrepareIOSSetupOperation'
        ], project.prepareSetup.prepareSetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'VerifyBaseSetupOperation',
            'VerifyIOSSetupOperation'
        ], project.verifySetup.verifySetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'ShowBaseSetupOperation',
            'ShowIOSSetupOperation'
        ], project.showSetup.showSetupOperations.collect { it.class.simpleName } )
    }
}
