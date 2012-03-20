package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin;
import com.apphance.ameba.ios.plugins.fonemonkey.FoneMonkeyPlugin

class TestFoneMonkeyTasks extends AbstractBaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(FoneMonkeyPlugin.class)
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
            'unlockKeyChain',
			'copySources'
        ],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }

    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareSetup',
            'verifySetup',
            'showSetup',
            'showConventions',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
        assertEquals([
            'PrepareIOSSetupOperation',
            'PrepareFoneMonkeySetupOperation',
        ], project.prepareSetup.prepareSetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'VerifyIOSSetupOperation',
            'VerifyFoneMonkeySetupOperation',
        ], project.verifySetup.verifySetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'ShowIOSSetupOperation',
            'ShowFoneMonkeySetupOperation',
        ], project.showSetup.showSetupOperations.collect { it.class.simpleName } )
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testFonemMonkeyTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareFoneMonkeyTemplates',
            'prepareFoneMonkeyReport',
            'buildFoneMonkeyRelease',
            'runMonkeyTests',
            'runSingleFoneMonkeyTest',
        ],AmebaCommonBuildTaskGroups.AMEBA_FONEMONKEY)
    }
}
