package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin;
import com.apphance.ameba.ios.plugins.release.IOSReleasePlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin;
import com.apphance.ameba.vcs.plugins.mercurial.MercurialPlugin

class TestReleaseIOSTasks extends AbstractBaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(MercurialPlugin.class)
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(ProjectReleasePlugin.class)
        project.project.plugins.apply(IOSReleasePlugin.class)
        return project
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanRelease',
            'updateVersion',
            'buildDocumentationZip',
            'buildSourcesZip',
            'prepareAvailableArtifactsInfo',
            'prepareForRelease',
            'prepareImageMontage',
            'prepareMailMessage',
            'sendMailMessage',
            'verifyReleaseNotes'
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
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
            'PrepareBaseSetupOperation',
            'PrepareMercurialSetupOperation',
            'PrepareIOSSetupOperation',
            'PrepareReleaseSetupOperation',
        ], project.prepareSetup.prepareSetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'VerifyBaseSetupOperation',
            'VerifyMercurialSetupOperation',
            'VerifyIOSSetupOperation',
            'VerifyReleaseSetupOperation',
        ], project.verifySetup.verifySetupOperations.collect { it.class.simpleName } )
        assertEquals([
            'ShowBaseSetupOperation',
            'ShowMercurialSetupOperation',
            'ShowIOSSetupOperation',
            'ShowReleaseSetupOperation',
        ], project.showSetup.showSetupOperations.collect { it.class.simpleName } )
    }
}
