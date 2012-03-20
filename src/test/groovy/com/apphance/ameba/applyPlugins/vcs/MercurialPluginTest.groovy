package com.apphance.ameba.applyPlugins.vcs;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.applyPlugins.android.BaseTaskTest
import com.apphance.ameba.vcs.plugins.mercurial.MercurialPlugin;

class MercurialPluginTest extends BaseTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(MercurialPlugin.class)
        return project
    }

    @Test
    public void testMercurialTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanVCS',
            'saveReleaseInfoInVCS',
        ],AmebaCommonBuildTaskGroups.AMEBA_VERSION_CONTROL)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareSetup',
            'verifySetup',
            'showSetup',
            'showConventions',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
        assertEquals([
            'PrepareMercurialSetupOperation',
        ], project.prepareSetup.prepareSetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'VerifyMercurialSetupOperation',
        ], project.verifySetup.verifySetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'ShowMercurialSetupOperation',
        ], project.showSetup.showSetupOperations.collect { it.class.simpleName } )
    }
}
