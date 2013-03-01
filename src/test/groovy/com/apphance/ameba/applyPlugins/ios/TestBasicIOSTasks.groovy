package com.apphance.ameba.applyPlugins.ios

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import org.gradle.api.Project
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue;

class TestBasicIOSTasks extends AbstractBaseIOSTaskTest {

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'clean',
                'buildAll',
                'buildAllSimulators',
                'build-GradleXCode-BasicConfiguration',
                'buildSingleVariant',
                'copyMobileProvision',
                'unlockKeyChain',
                'copySources',
                'copyDebugSources',
        ], AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }

    @Test
    public void testConfigurationTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'cleanConfiguration',
                'readProjectConfiguration',
                'readIOSProjectConfiguration',
                'readIOSParametersFromXcode',
                'readIOSProjectVersions',
        ], AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'prepareSetup',
                'verifySetup',
                'showSetup',
                'showConventions'
        ], AmebaCommonBuildTaskGroups.AMEBA_SETUP)
        assertTrue('Prepare setup for iOS contains all expected operations',
                'PrepareIOSSetupOperation' in project.prepareSetup.prepareSetupOperations*.class.simpleName)
        assertTrue('Verify setup for iOS contains all expected operations',
                'VerifyIOSSetupOperation' in project.verifySetup.verifySetupOperations*.class.simpleName)
        assertTrue('Show setup for iOS contains all expected operations',
                'ShowIOSSetupOperation' in project.showSetup.showSetupOperations*.class.simpleName)
    }
}
