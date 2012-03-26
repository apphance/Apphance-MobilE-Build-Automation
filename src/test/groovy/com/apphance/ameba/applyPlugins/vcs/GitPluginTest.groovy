package com.apphance.ameba.applyPlugins.vcs;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.BaseTaskTest;
import com.apphance.ameba.vcs.plugins.git.GitPlugin;

class GitPluginTest extends BaseTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(GitPlugin.class)
        return project
    }

    @Test
    public void testGitTasksAvailable() {
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
            'PrepareGitSetupOperation',
        ], project.prepareSetup.prepareSetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'VerifyGitSetupOperation',
        ], project.verifySetup.verifySetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'ShowGitSetupOperation',
        ], project.showSetup.showSetupOperations.collect { it.class.simpleName } )
    }
}
